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
    glossary_id BIGINT       NOT NULL REFERENCES glossaries (id) ON DELETE CASCADE,
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
    creator_id                 BIGINT    NOT NULL REFERENCES admin_users (id) ON DELETE RESTRICT,
    gender_id                  BIGINT    NOT NULL REFERENCES glossary_values (id) ON DELETE RESTRICT,
    age                        INTEGER   NOT NULL CHECK (age > 0),
    height                     NUMERIC(5, 1),
    weight                     NUMERIC(5, 1),
    hip_measurement            NUMERIC(5, 1),
    alcohol_id                 BIGINT    REFERENCES glossary_values (id) ON DELETE RESTRICT,
    profession_id              BIGINT    REFERENCES glossary_values (id) ON DELETE RESTRICT,
    region_id                  BIGINT    REFERENCES glossary_values (id) ON DELETE RESTRICT,
    glucose                    NUMERIC(6, 2),
    cholesterol                NUMERIC(6, 2),
    non_hdl_cholesterol        NUMERIC(6, 2),
    vldl_cholesterol           NUMERIC(6, 2),
    hdl_cholesterol            NUMERIC(6, 2),
    ldl_cholesterol            NUMERIC(6, 2),
    apolipoprotein_a           NUMERIC(6, 2),
    apolipoprotein_b           NUMERIC(6, 2),
    triglycerides              NUMERIC(6, 2)
);

CREATE INDEX idx_patients_creator_id    ON patients (creator_id);
CREATE INDEX idx_patients_gender_id     ON patients (gender_id);
CREATE INDEX idx_patients_alcohol_id    ON patients (alcohol_id);
CREATE INDEX idx_patients_profession_id ON patients (profession_id);
CREATE INDEX idx_patients_region_id     ON patients (region_id);

-- ==========================================
-- Диагнозы пациентов
-- ==========================================

CREATE TABLE diagnoses
(
    id               BIGSERIAL PRIMARY KEY,
    patient_id       BIGINT    NOT NULL REFERENCES patients (id) ON DELETE CASCADE,
    diagnosis_id     BIGINT    NOT NULL REFERENCES glossary_values (id) ON DELETE RESTRICT,
    year_of_diagnosis SMALLINT CHECK (year_of_diagnosis BETWEEN 1900 AND EXTRACT(YEAR FROM CURRENT_DATE))
);
