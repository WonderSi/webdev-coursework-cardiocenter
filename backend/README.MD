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
    expiration-ms: 86400000           # 24 часа
```

**Claims токена:**
- `sub` — username
- `iat` — время выдачи
- `exp` — время истечения
- `roles` — список ролей (`List<String>`)

---

## Миграции БД (Flyway)

Расположены: `src/main/resources/db/migration/`

| Файл | Описание |
|---|---|
| `V1__init.sql` | Создание таблиц: `glossaries`, `glossary_values`, `admin_users`, `patients`, `diagnoses` |
| `V2__seed_glossaries.sql` | Заполнение справочников начальными данными |

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

