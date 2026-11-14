-- ==========================================
-- Поля обследования для моделей city / region
-- ==========================================
ALTER TABLE patients
    -- Тип модели выбирается отдельно от района проживания.
    ADD COLUMN model_kind VARCHAR(10) NOT NULL,
    -- Период для city; для region не указывается.
    ADD COLUMN model_year INTEGER,
    -- Хронические заболевания лёгких: обязательный ответ 0 — нет, 1 — есть.
    ADD COLUMN chronic_lung_disease INTEGER NOT NULL,
    -- Для city нужен период 2013/2017/2019/2021, для region период должен быть NULL.
    ADD CONSTRAINT chk_patients_model_fields CHECK (
        chronic_lung_disease IN (0, 1)
        AND ((model_kind = 'city' AND model_year IS NOT NULL AND model_year IN (2013, 2017, 2019, 2021))
             OR (model_kind = 'region' AND model_year IS NULL))
    );

-- ==========================================
-- Профессии
-- ==========================================
DELETE FROM glossary_values
WHERE glossary_id = (SELECT id FROM glossaries WHERE name = 'profession');

INSERT INTO glossary_values (glossary_id, code, value) VALUES
((SELECT id FROM glossaries WHERE name = 'profession'), 1, 'Руководители и представители власти'),
((SELECT id FROM glossaries WHERE name = 'profession'), 2, 'Специалисты высшего уровня квалификации'),
((SELECT id FROM glossaries WHERE name = 'profession'), 3, 'Специалисты среднего уровня квалификации'),
((SELECT id FROM glossaries WHERE name = 'profession'), 4, 'Служащие по подготовке и оформлению документации'),
((SELECT id FROM glossaries WHERE name = 'profession'), 5, 'Работники сферы услуг, охраны и продавцы'),
((SELECT id FROM glossaries WHERE name = 'profession'), 6, 'Квалифицированные работники сельского/лесного хозяйства, рыбоводства'),
((SELECT id FROM glossaries WHERE name = 'profession'), 7, 'Рабочие промышленных предприятий, строительства и транспорта'),
((SELECT id FROM glossaries WHERE name = 'profession'), 8, 'Операторы установок и машин, сборщики'),
((SELECT id FROM glossaries WHERE name = 'profession'), 9, 'Неквалифицированные рабочие'),
((SELECT id FROM glossaries WHERE name = 'profession'), 10, 'Военнослужащие'),
((SELECT id FROM glossaries WHERE name = 'profession'), 11, 'Пенсионер'),
((SELECT id FROM glossaries WHERE name = 'profession'), 12, 'Ведение домашнего хозяйства'),
((SELECT id FROM glossaries WHERE name = 'profession'), 13, 'Другое');
