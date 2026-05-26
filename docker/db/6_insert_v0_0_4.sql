INSERT INTO directions (name) VALUES ('Система мониторинга');
INSERT INTO directions (name) VALUES ('Сбор данных с приборов');


INSERT INTO projects (name, direction_id)
VALUES ('Анализ поступающих данных', 1);

INSERT INTO projects (name, direction_id)
VALUES ('Формирование отчетов', 1);

INSERT INTO projects (name, direction_id)
VALUES ('Отправка данных для анализа', 1);


INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id)
VALUES ('Обработать поток данных с датчиков', 'PENDING', NOW(), '2026-06-01 10:00:00', NULL, 0, 1, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id)
VALUES ('Сформировать ежедневный отчет', 'PENDING', NOW(), '2026-06-02 10:00:00', NULL, 0, 1, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id)
VALUES ('Отправить данные в аналитическую систему', 'COMPLETED_ON_TIME', NOW(), '2026-06-03 10:00:00', '2026-06-03 12:00:00', 5, 2, 1);