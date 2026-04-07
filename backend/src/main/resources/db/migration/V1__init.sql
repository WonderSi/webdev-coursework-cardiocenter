-- ==========================================
-- Словари
-- ==========================================

CREATE TABLE glossaries
(
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE glossary_values
(
    id          BIGSERIAL    PRIMARY KEY,
    glossary_id BIGINT       NOT NULL REFERENCES glossaries (id),
    code        INTEGER      NOT NULL,
    value       VARCHAR(255) NOT NULL,
    UNIQUE (glossary_id, code)
);

-- ==========================================
-- Администраторы
-- ==========================================

CREATE TABLE admin_users
(
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'DOCTOR'
);

-- ==========================================
-- Пациенты (данные из Excel-импорта)
-- ==========================================

CREATE TABLE patients
(
    id                         BIGSERIAL PRIMARY KEY,
    create_date                DATE      NOT NULL DEFAULT CURRENT_DATE,
    creator_id                 BIGINT    REFERENCES admin_users (id),
    gender                     SMALLINT  NOT NULL CHECK (gender IN (1, 2)),
    age                        INTEGER   NOT NULL CHECK (age > 0),
    height                     NUMERIC(5, 1),
    weight                     NUMERIC(5, 1),
    hip_measurement            NUMERIC(5, 1),
    alcohol                    SMALLINT  CHECK (alcohol IN (1, 2, 3)),
    profession                 SMALLINT  CHECK (profession BETWEEN 1 AND 14),
    region                     SMALLINT  CHECK (region BETWEEN 1 AND 6),
    glucose                    NUMERIC(6, 2),
    cholesterol                NUMERIC(6, 2),
    non_hdl_cholesterol        NUMERIC(6, 2),
    vldl_cholesterol           NUMERIC(6, 2),
    hdl_cholesterol            NUMERIC(6, 2),
    ldl_cholesterol            NUMERIC(6, 2),
    apolipoprotein_a           NUMERIC(6, 2),
    apolipoprotein_b           NUMERIC(6, 2),
    triglycerides              NUMERIC(6, 2),
    stroke                     BOOLEAN,
    stroke_year                INTEGER,
    heart_failure              BOOLEAN,
    heart_failure_year         INTEGER,
    cad_chd_ihd                BOOLEAN,
    cad_chd_ihd_year           INTEGER,
    angina                     BOOLEAN,
    angina_year                INTEGER,
    myocardial_infarction      BOOLEAN,
    myocardial_infarction_year INTEGER,
    arterial_hypertension      BOOLEAN,
    arterial_hypertension_year INTEGER
);

-- ==========================================
-- Диагнозы пациентов
-- ==========================================

CREATE TABLE diagnoses
(
    id               BIGSERIAL PRIMARY KEY,
    patient_id       BIGINT    NOT NULL REFERENCES patients (id) ON DELETE CASCADE,
    diagnosis_id     BIGINT    NOT NULL REFERENCES glossary_values (id),
    year_of_diagnosis SMALLINT CHECK (year_of_diagnosis BETWEEN 1900 AND EXTRACT(YEAR FROM CURRENT_DATE))
);
