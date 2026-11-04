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
`POST /api/auth/logout` удаляет обе cookie с исходными Path и флагами.
Серверный отзыв refresh при выходе (F-04) будет добавлен отдельным этапом.

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

Для этого маршрута настроен [CSRF-механизм Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
с `CookieCsrfTokenRepository` и стандартной маскировкой токена. HttpSession
не используется. CSRF-политика остальных изменяющих маршрутов остаётся отдельным
решением архитектуры перед внешним запуском; их текущий контракт этим этапом
не меняется.

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
- Redis отключён в тестовом профиле (`exclude auto-configuration`)
- Flyway-миграции применяются автоматически перед тестами
- Базовый класс интеграционных тестов: `BaseIntegrationTest.kt`

---

## Что ещё не реализовано

- REST-контроллеры (`@RestController`)
- JPA-репозитории (`JpaRepository`)
- Сервисный слой (кроме `JwtService`)
- Spring Security фильтры / конфигурация `SecurityFilterChain`
- Использование Redis-кэша (`@Cacheable`)
- Интеграция с ML-сервисом\
- Создание тестов

