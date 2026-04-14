-- ==========================================
-- Пол
-- ==========================================
INSERT INTO glossaries (name) VALUES ('gender');
INSERT INTO glossary_values (glossary_id, code, value) VALUES
    ((SELECT id FROM glossaries WHERE name = 'gender'), 1, 'Мужской'),
    ((SELECT id FROM glossaries WHERE name = 'gender'), 2, 'Женский');

-- ==========================================
-- Алкоголь
-- ==========================================
INSERT INTO glossaries (name) VALUES ('alcohol');
INSERT INTO glossary_values (glossary_id, code, value) VALUES
    ((SELECT id FROM glossaries WHERE name = 'alcohol'), 1, 'Никогда не употреблял'),
    ((SELECT id FROM glossaries WHERE name = 'alcohol'), 2, 'Употреблял ранее'),
    ((SELECT id FROM glossaries WHERE name = 'alcohol'), 3, 'Употребляю');
    

-- ==========================================
-- Профессия
-- ==========================================
INSERT INTO glossaries (name) VALUES ('profession');
INSERT INTO glossary_values (glossary_id, code, value) VALUES
((SELECT id FROM glossaries WHERE name = 'profession'), 1,  'Ведение домашнего хозяйства'),
((SELECT id FROM glossaries WHERE name = 'profession'), 2,  'Вооруженные силы'),
((SELECT id FROM glossaries WHERE name = 'profession'), 3,  'Лица свободных профессий'),
((SELECT id FROM glossaries WHERE name = 'profession'), 4,  'Низкоквалифицированные и неквалифицированные работники, рабочие, ручной труд'),
((SELECT id FROM glossaries WHERE name = 'profession'), 5,  'Операторы и монтажники установок и машинного оборудования'),
((SELECT id FROM glossaries WHERE name = 'profession'), 6,  'Служащие, сфера обслуживания, работники среднего звена'),
((SELECT id FROM glossaries WHERE name = 'profession'), 7,  'Никогда не работающие домохозяйки'),
((SELECT id FROM glossaries WHERE name = 'profession'), 8,  'Дипломированные специалисты, умственный труд'),
((SELECT id FROM glossaries WHERE name = 'profession'), 9,  'Другое'),
((SELECT id FROM glossaries WHERE name = 'profession'), 10, 'Квалифицированные специалисты сельского хозяйства и рыболовного'),
((SELECT id FROM glossaries WHERE name = 'profession'), 11, 'Пенсионеры'),
((SELECT id FROM glossaries WHERE name = 'profession'), 12, 'Ремесленники и представители других отраслей промышленности'),
((SELECT id FROM glossaries WHERE name = 'profession'), 13, 'Техники и младшие специалисты'),
((SELECT id FROM glossaries WHERE name = 'profession'), 14, 'Представители законодат. органов власти, высокопост. долж. лица, менеджеры и руководители');

-- ==========================================
-- Район / Регион
-- ==========================================
INSERT INTO glossaries (name) VALUES ('region');
INSERT INTO glossary_values (glossary_id, code, value) VALUES
    ((SELECT id FROM glossaries WHERE name = 'region'), 1, 'Рудничный'),
    ((SELECT id FROM glossaries WHERE name = 'region'), 2, 'Центральный'),
    ((SELECT id FROM glossaries WHERE name = 'region'), 3, 'Заводский'),
    ((SELECT id FROM glossaries WHERE name = 'region'), 4, 'Кировский'),
    ((SELECT id FROM glossaries WHERE name = 'region'), 5, 'Ленинский'),
    ((SELECT id FROM glossaries WHERE name = 'region'), 6, 'Сельская местность');

-- ==========================================
-- Диагнозы
-- (используется в таблице diagnoses)
-- ==========================================
INSERT INTO glossaries (name) VALUES ('diagnosis');
INSERT INTO glossary_values (glossary_id, code, value) VALUES
    ((SELECT id FROM glossaries WHERE name = 'diagnosis'), 1, 'Инсульт'),
    ((SELECT id FROM glossaries WHERE name = 'diagnosis'), 2, 'Сердечная недостаточность'),
    ((SELECT id FROM glossaries WHERE name = 'diagnosis'), 3, 'Нарушение ритма / ИБС'),
    ((SELECT id FROM glossaries WHERE name = 'diagnosis'), 4, 'Стенокардия'),
    ((SELECT id FROM glossaries WHERE name = 'diagnosis'), 5, 'Инфаркт миокарда'),
    ((SELECT id FROM glossaries WHERE name = 'diagnosis'), 6, 'Артериальная гипертензия');
