# Бэкенд

## Стек технологий

| Категория | Технология |
|---|---|
| Язык | Kotlin 1.9.25 |
| Java | 21 |
| Фреймворк | Spring Boot 3.4.3 |
| Сборка | Maven |
| БД | PostgreSQL 16 |
| Кэш | Redis 7 |
| Миграции | Flyway |
| ORM | Hibernate / Spring Data JPA |
| Безопасность | Spring Security 6 + JWT (jjwt 0.12.6) |
| Документация API | SpringDoc OpenAPI (Swagger) 2.8.5 |
| Тестирование | JUnit 5, Testcontainers, Spring Security Test |

---

## Структура проекта

```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/com/medical/cardio/
│   │   │   ├── CardioApplication.kt       # точка входа
│   │   │   ├── entity/                    # JPA-сущности
│   │   │   └── security/                  # JWT-сервис
│   │   └── resources/
│   │       ├── application.yml            # основной конфиг
│   │       ├── application-dev.yml        # dev-профиль
│   │       └── db/migration/              # Flyway миграции
│   └── test/
│       ├── kotlin/com/medical/cardio/
│       └── resources/application.yml
├── docker-compose.yaml
└── pom.xml
```

---

## Сущности (JPA Entities)

### `Patient` — таблица `patients`
Основная сущность. Хранит неизменяемые медицинские данные пациента.

| Поле | Тип | Описание |
|---|---|---|
| `id` | Long | PK |
| `createDate` | LocalDate | Дата создания записи |
| `creator` | AdminUser | FK — кто создал запись |
| `gender` | Short | 1 = Муж, 2 = Жен |
| `age` | Int | Возраст |
| `height` / `weight` | BigDecimal | Рост / вес |
| `hipMeasurement` | BigDecimal | Обхват талии/бедер |
| `alcohol` | Short | Употребление алкоголя (код из глоссария) |
| `profession` | Short | Профессия (код 1–14) |
| `region` | Short | Регион (код 1–6) |
| `glucose` | BigDecimal | Глюкоза |
| `cholesterol` | BigDecimal | Общий холестерин |
| `hdlCholesterol` | BigDecimal | ЛПВП |
| `ldlCholesterol` | BigDecimal | ЛПНП |
| `vldlCholesterol` | BigDecimal | ЛПОНП |
| `nonHdlCholesterol` | BigDecimal | Не-ЛПВП холестерин |
| `apolipoproteinA` / `apolipoproteinB` | BigDecimal | Аполипопротеины A/B |
| `triglycerides` | BigDecimal | Триглицериды |
| `stroke`, `heartFailure`, `cadChdIhd`, `angina`, `myocardialInfarction`, `arterialHypertension` | Boolean | Анамнез сердечно-сосудистых заболеваний |
| `*Year` | Short | Год постановки соответствующего диагноза |

---

### `AdminUser` — таблица `admin_users`
Учётная запись администратора системы.

| Поле | Тип | Описание |
|---|---|---|
| `id` | Long | PK |
| `username` | String (50) | Уникальный логин |
| `passwordHash` | String (255) | Хэш пароля (bcrypt) |

---

### `Diagnosis` — таблица `diagnoses`
Связывает пациента с диагнозом.

| Поле | Тип | Описание |
|---|---|---|
| `id` | Long | PK |
| `patient` | Patient | FK (ManyToOne, lazy) |
| `diagnosis` | GlossaryValue | FK на тип диагноза |
| `yearOfDiagnosis` | Short | Год постановки |

---

### `Glossary` — таблица `glossaries`
Справочник (например: "gender", "profession").

| Поле | Тип | Описание |
|---|---|---|
| `id` | Long | PK |
| `name` | String (100) | Уникальное имя справочника |
| `values` | List\<GlossaryValue\> | OneToMany (lazy) |

---

### `GlossaryValue` — таблица `glossary_values`
Значение внутри справочника.

| Поле | Тип | Описание |
|---|---|---|
| `id` | Long | PK |
| `glossary` | Glossary | FK (ManyToOne, lazy) |
| `code` | Int | Числовой код значения |
| `value` | String (255) | Отображаемое название |

---

## Безопасность — `JwtService`

Расположен: `security/JwtService.kt`

Отвечает за генерацию и валидацию JWT-токенов.

**Ключевые методы:**

| Метод | Назначение |
|---|---|
| `generateToken(userDetails)` | Генерирует JWT с ролями пользователя |
| `generateToken(extraClaims, userDetails)` | Генерирует JWT с дополнительными claims |
| `isTokenValid(token, userDetails)` | Проверяет подпись и соответствие пользователю |
| `extractUsername(token)` | Извлекает имя пользователя из токена |
| `extractRoles(token)` | Извлекает роли из claims |

**Конфигурация** (в `application.yml`):
```yaml
app:
  jwt:
    secret: <base64-encoded-secret>   # минимум 256 бит / 32 байта
    expiration-ms: 900000             # 15 минут; JWT_EXPIRATION_MS переопределяет основной конфиг
```

**Claims токена:**
- `sub` — username
- `iat` — время выдачи
- `exp` — время истечения
- `roles` — список ролей (`List<String>`)

---

## Выдача refresh-сессий — `RefreshTokenService`

`issueToken(user, lifetime, rememberMe)` создаёт случайный refresh token из 32 байт,
сохраняет его SHA-256-хэш, сотрудника, режим rememberMe, время создания и истечения в
`refresh_tokens`. Новый токен не отозван: `revoked_date` равен `NULL`.
Продолжительность `lifetime` должна быть положительной.

Результат `IssuedRefreshToken` содержит исходный токен и момент истечения для
последующей установки cookie. Вызов выполняется в транзакции. Для поиска сессии
используется `RefreshTokenRepository.findByTokenHash`.

`AuthService.login` проверяет credentials и актуальное состояние сотрудника,
выдаёт access и вызывает `issueToken` в одной транзакции. Контроллер возвращает
email и роль в JSON, токены передаются двумя отдельными заголовками `Set-Cookie`.

| Cookie | Path | Без `rememberMe` | С `rememberMe` |
|---|---|---|---|
| `access_token` | `/` | Сессионная, JWT действует 15 минут | Постоянная на 15 минут |
| `refresh_token` | `/api/auth` | Сессионная, запись в БД действует 1 день | Постоянная на 30 дней |

У обеих cookie установлены `HttpOnly` и `SameSite=Strict`. `AuthCookieService`
использует `app.auth-cookie.secure`: в `prod` это `true`, в `dev` и при
локальном HTTP-запуске с профилями `prod,local` — `false`.

Сроки refresh задаются настройками `app.refresh-token.expiration-ms` и
`app.refresh-token.remember-me-expiration-ms`; в основном конфиге их можно
переопределить через `REFRESH_TOKEN_EXPIRATION_MS` и
`REFRESH_TOKEN_REMEMBER_ME_EXPIRATION_MS`. Значения должны быть положительными,
а срок с запоминанием — не короче обычного. `RefreshTokenConfig.lifetimeFor`
возвращает выбранную продолжительность как `Duration`.

При неверных credentials или блокировке новые refresh-сессии и cookie не выдаются.
`POST /api/auth/logout` отзывает текущую refresh-сессию и удаляет обе cookie
с исходными Path и флагами. Для запроса нужен CSRF-токен, как описано ниже.

### Обновление access через refresh cookie

1. Выполнить `GET /api/auth/csrf` с передачей cookie. Ответ содержит `headerName`
   и `token`; браузер сохраняет HttpOnly cookie `XSRF-TOKEN` с Path=/api/auth.
   Ответ имеет `Cache-Control: no-store`.
2. Выполнить `POST /api/auth/refresh` с cookie и заголовком, указанным в
   `headerName`, передав в него значение `token` из JSON. JavaScript не должен
   читать HttpOnly cookie — нужное значение уже есть в ответе bootstrap-маршрута.
3. При успехе получить `200`, текущие email и роль в JSON и новую access cookie.
   Действующий access для этого не требуется: отсутствующий, истёкший или
   недействительный access не мешает проверке refresh.

```javascript
const csrfResponse = await fetch('/api/auth/csrf', { credentials: 'include' });
const csrf = await csrfResponse.json();
const response = await fetch('/api/auth/refresh', {
  method: 'POST',
  credentials: 'include',
  headers: { [csrf.headerName]: csrf.token },
});
// 200: повторить исходный запрос. 401: предложить вход.
// 403 с CSRF_TOKEN_INVALID: получить новый CSRF-токен и повторить обновление.
```

`RefreshTokenService.validateToken` проверяет хэш, срок, отсутствие отзыва и
блокировки сотрудника. Недопустимый refresh даёт `401 application/problem+json`
с кодом `REFRESH_TOKEN_INVALID`. Отсутствующий или неверный CSRF даёт `403`
с кодом `CSRF_TOKEN_INVALID`, до проверки refresh. Cookie и роли формируются
из серверного состояния, токены в JSON не возвращаются.

Refresh token при обновлении не заменяется, новые сессии не создаются, исходный
срок в БД и refresh cookie не продлеваются. Миграция V7 сохраняет режим
`remember_me`, чтобы новая access cookie оставалась сессионной либо постоянной
согласно исходному входу. Для записей, существовавших до V7, флаг по умолчанию
равен `false`; новый вход сохраняет выбранный режим явно.

Для обновления и выхода настроен [CSRF-механизм Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
с `CookieCsrfTokenRepository` и стандартной маскировкой токена. HttpSession
не используется. CSRF-политика остальных изменяющих маршрутов остаётся отдельным
решением архитектуры перед внешним запуском; их текущий контракт этим этапом
не меняется.

### Выход и отзыв текущей refresh-сессии — F-04

Получите `GET /api/auth/csrf`, затем выполните `POST /api/auth/logout`
с cookie и заголовком `headerName: token` из ответа bootstrap-маршрута.
Это тот же протокол, что у обновления access. При успехе возвращаются `200`
с пустым телом, `Cache-Control: no-store` и два заголовка `Set-Cookie`:
`access_token` с Path=/ и `refresh_token` с Path=/api/auth, оба с Max-Age=0.
Флаги HttpOnly, SameSite и Secure соответствуют выдаче cookie.

`AuthService.logout` вызывает `RefreshTokenService.revokeToken` в транзакции.
`RefreshTokenRepository.revokeByTokenHash` выполняет один условный UPDATE по
уникальному хэшу: `revokedDate` заполняется только при NULL. Это сохраняет
первоначальное время при повторном или параллельном выходе. Запись сессии и
её исходный срок сохраняются; отозванный refresh больше не обновляет access.
Новая миграция для F-04 не требуется: поле отзыва создано в V6.

Отзывается только refresh из текущей cookie. Другие сессии этого сотрудника
и других сотрудников продолжают работать. Неизвестный, пустой или отсутствующий
refresh не мешает успешной очистке cookie. Истёкшая сессия и заблокированная
учётная запись также могут быть завершены; действующий access для выхода
не требуется. Копия ранее выданного access остаётся действительной до своего
истечения, если сотрудник не заблокирован — так определена модель F-03/F-04.

Без корректного CSRF запрос даёт `403 CSRF_TOKEN_INVALID`, не отзывает сессию
и не очищает авторизационные cookie. При ошибке сохранения отзыва возвращается
безопасный `500`.

---

## Миграции БД (Flyway)

Расположены: `src/main/resources/db/migration/`

| Файл | Описание |
|---|---|
| `V1__init.sql` | Создание таблиц: `glossaries`, `glossary_values`, `admin_users`, `patients`, `diagnoses` |
| `V2__seed_glossaries.sql` | Заполнение справочников начальными данными |
| `V3__rename_username_to_email.sql` | Переименование логина в email |
| `V4__replace_doctor_extended_with_super_admin.sql` | Роль SUPER_ADMIN |
| `V5__add_admin_user_blocked.sql` | Блокировка сотрудника |
| `V6__create_refresh_tokens.sql` | Хранение refresh-сессий и хэшей токенов |
| `V7__add_refresh_token_remember_me.sql` | Сохранение режима rememberMe для обновления access cookie |

### Данные справочников (V2)

| Справочник | Значения |
|---|---|
| `gender` | Мужской, Женский |
| `alcohol` | Употребляет / Употреблял / Никогда |
| `profession` | 14 категорий (законодатели → прочие) |
| `region` | Сельская местность, Центральный, Шахтёрский, Заводской, Кировский, Ленинский |
| `diagnosis` | Инсульт, Сердечная недостаточность, Аритмия/ИБС, Стенокардия, ИМ, Гипертония |

---

## Конфигурация

### `application.yml` (основной)
- Активный профиль: `dev`
- DDL auto: `validate` (схема только проверяется, не создаётся)
- JSON naming: `SNAKE_CASE`, null-поля не сериализуются
- Actuator endpoints: `health`, `metrics`, `info`
- ML-сервис URL: `${ML_SERVICE_URL:http://localhost:8000}`

### Профиль preview: подготовка healthcheck

Дополнительный `application-preview.yml` предназначен для временных стендов.
Он оставляет в HTTP exposure только `health` и скрывает детали и компоненты
ответа. Проверки БД/Redis при этом сохраняются.
Планируемая комбинация для контейнера — `prod,preview`.
В preview env-примере `APP_AUTHCOOKIE_SECURE=false` переопределяет
`app.auth-cookie.secure` для локального HTTP; для HTTPS требуется `true`.
Оба значения проверены `AuthCookieProfileTest` через переменные окружения.
Правило анонимного `GET /actuator/health` реализуется отдельной
`PreviewHealthSecurityConfig` с `@Profile("preview")`.
Профиль и класс проверены `PreviewHealthSecurityConfigTest`: в preview
анонимный GET health возвращает только общий статус, неисправность даёт 503;
остальные проверенные маршруты сохраняют защиту. Без preview health требует
авторизации. Локальный preview Backend также проверен запуском в Docker:
миграции1–7, bootstrap, health, вход/refresh/logout и семь прогнозов через ML.
Frontend подключён, HTTP через nginx проверен. Загрузка страницы и опрос
в браузере подтверждены пользователем, как и вход с обычным F5.
Stop/up с сохранением данных проверен. Свежий access сохраняется после
рестарта Backend. Наблюдавшийся позже browser401 вызван истечением15мин;
Frontend пока не вызывает refresh автоматически.
Состояние и код: [план preview](../docs/PREVIEW_ENVIRONMENTS_PLAN.md).

### Настройки ограничения входа — F-35

`LoginRateLimitConfig` читает настройки из `app.login-rate-limit`.
Период и оба лимита должны быть строго положительными; недопустимые значения
вызывают ошибку при создании конфигурации.

| Переменная окружения | Настройка | По умолчанию |
|---|---|---|
| `LOGIN_RATE_LIMIT_WINDOW_MS` | `window-ms` — период подсчёта в миллисекундах | `60000` |
| `LOGIN_RATE_LIMIT_MAX_ATTEMPTS_PER_IP` | `max-attempts-per-ip` — лимит по IP | `20` |
| `LOGIN_RATE_LIMIT_MAX_ATTEMPTS_PER_LOGIN` | `max-attempts-per-login` — лимит по введённому логину | `5` |

Переменные описаны в `docker/.env.example` относительно корня репозитория
и передаются Backend в обоих Docker Compose-файлах. В тестовых конфигурациях
значения заданы явно. На этом этапе подготовлены настройки, их валидация
и обработка ошибки превышения лимита. Для счётчика подготовлены Lua-скрипт,
его конфигурация и `RedisLoginAttemptCounter`: Kotlin-класс вызывает скрипт
и проверяет полученные количество попыток и остаток времени. Unit-тесты
проверяют допустимые ответы и отказ при отсутствии результата, неправильном
размере списка, типах и значениях. Интеграционные тесты проверяют реальные
вызовы Lua, сохранение срока жизни при повторной попытке, истечение и новый
отсчёт, независимость ключей и восемь параллельных увеличений без потери попыток.
Каждый сценарий сам запускает Redis 7 через Docker CLI, использует случайный
порт на `127.0.0.1` и удаляет контейнер после выполнения. Для запуска нужны
Docker CLI и работающий Docker.

`LoginRateLimitService.checkLoginAttempt` сначала проверяет счётчик IP,
затем счётчик email. При превышении лимита IP до счётчика email выполнение
не доходит. Ключи имеют разные префиксы и SHA-256-представления идентификаторов.
Для ключа email применяются `trim` и нижний регистр с `Locale.ROOT`;
исходный email для аутентификации остаётся прежним. Поиска пользователя в БД
при проверке лимитов нет. Попытка учитывается до сравнения с лимитом;
точное значение лимита допускается, превышение вызывает отказ.
Время ожидания округляется вверх до секунд, минимум — одна секунда.
Счётчик и сервис зарегистрированы в Spring. `AuthController.login` вызывает
проверку с `httpRequest.remoteAddr` и исходным email до `AuthService.login`:
при отказе проверка пароля и создание refresh-сессии не выполняются.
Учитываются все попытки с прошедшим валидацию телом, включая успешный вход;
успешный вход не сбрасывает счётчики. Некорректное тело возвращает `400`
до вызова ограничителя. Cookie выдаются только после успешной аутентификации.

Адрес клиента для ограничения нужно получать через `request.remoteAddr`.
Tomcat работает с `server.forward-headers-strategy: native` и учитывает
`X-Real-IP` только от прокси, адрес которого соответствует `TRUSTED_PROXY_REGEX`.
Production nginx перезаписывает этот заголовок значением `$remote_addr`.
По умолчанию используется `(?!)`: доверенных прокси нет. Пустая переменная
также не включает доверие всем адресам благодаря группе вокруг значения
в `internal-proxies`. До явной настройки за nginx будет виден адрес самого nginx.
Для production нужно определить его фактический адрес со стороны Backend,
задать точное регулярное выражение и актуализировать его при смене адреса.
Произвольные `X-Forwarded-For` и `Forwarded` не определяют IP клиента.
В локальном Compose используется Vite, который сейчас не выставляет
`X-Real-IP`; включение доверия к нему требует согласованного перезаписывания
заголовка на стороне Frontend. При прямом обращении к Backend доверие не нужно.

`LoginRateLimitExceededException` обрабатывается как `429 Too Many Requests`
с телом `application/problem+json`, кодом `LOGIN_RATE_LIMIT_EXCEEDED`,
типом `urn:cardiocenter:problem:login-rate-limit` и общим сообщением
«Повторите попытку позже». Заголовок `Retry-After` содержит время ожидания
в секундах, а `Cache-Control: no-store` запрещает кэшировать отказ.
Авторизационные cookie при этом не выдаются. В OpenAPI ответ описан компонентом
`LoginRateLimited` и указан только для `POST /api/auth/login`.

Ошибки соединения с Redis и таймауты проверки преобразуются в
`LoginRateLimitUnavailableException`: вход временно отклоняется с `503`,
кодом `SERVICE_UNAVAILABLE` и `Cache-Control: no-store`. Авторизационные
cookie не выдаются; технические сведения доступны в серверном логе.
Неожиданные ошибки обработки ответа не маскируются как недоступность Redis.
В OpenAPI ответ описан компонентом `ServiceUnavailable` у операции login.

Для запросов с разрешённого Frontend CORS открывает JavaScript доступ
к `Retry-After` через `Access-Control-Expose-Headers`. Разрешённые адреса
задаются в `ALLOWED_ORIGINS`; по умолчанию используется `http://localhost:5173`.
Ответ также содержит разрешение на передачу cookie (`Access-Control-Allow-Credentials: true`).

### `application-dev.yml` (dev-профиль)
```
PostgreSQL:  localhost:5432 / cardio_db / cardio_user / cardio_pass
Redis:       localhost:6379 / redis_pass
```
Логирование на уровне DEBUG для `com.medical`, Spring Security и Hibernate SQL.

---

## Запуск инфраструктуры (Docker)

```bash
docker-compose up -d
```

Запускает:
- PostgreSQL 16 (Alpine) — порт `5432`
- Redis 7 (Alpine) — порт `6379`

---

## Тесты

- Используют Testcontainers для поднятия реальной PostgreSQL-базы
- Автоконфигурация Redis отключена в тестовом профиле (`exclude auto-configuration`)
- `RedisLoginAttemptCounterIntegrationTest` создаёт подключение к отдельному
  Redis-контейнеру через `RedisTestContainer`; нужны Docker CLI и работающий Docker
- Flyway-миграции применяются автоматически перед тестами
- Базовый класс интеграционных тестов: `BaseIntegrationTest.kt`
- В наследниках `BaseIntegrationTest` счётчик попыток заменён через `@MockitoBean`
  и перед каждым тестом по умолчанию возвращает одну допустимую попытку.
  Сценарии ограничения в `AuthControllerTest` задают последовательность ответов
  или отказ счётчика: проверяются `401` → `429` для существующего и неизвестного
  email, блокировка верных credentials по IP/email и `503` без новой сессии.
  Реальная политика ограничения и PostgreSQL участвуют в этих тестах;
  выполнение Lua и истечение ключей отдельно проверяются с настоящим Redis.
- `UntrustedProxyClientIpHttpTest` и `TrustedProxyClientIpHttpTest` проверяют
  определение IP через настоящий HTTP-сервер Tomcat без PostgreSQL и Redis.
  Диагностический контроллер существует только в тестовом профиле.

Запуск тестов счётчика и политики ограничения входа из каталога `backend` в PowerShell (JDK 21):

```powershell
.\mvnw.cmd "-Dtest=RedisLoginAttemptCounterTest,RedisLoginAttemptCounterIntegrationTest,LoginRateLimitServiceTest" test
```

Проверка доверия к прокси из каталога `backend` (Docker не требуется):

```powershell
.\mvnw.cmd "-Dtest=UntrustedProxyClientIpHttpTest,TrustedProxyClientIpHttpTest" test
```

---

## Что ещё не реализовано

- REST-контроллеры (`@RestController`)
- JPA-репозитории (`JpaRepository`)
- Сервисный слой (кроме `JwtService`)
- Spring Security фильтры / конфигурация `SecurityFilterChain`
- Использование Redis-кэша (`@Cacheable`)
- Интеграция с ML-сервисом\
- Создание тестов

