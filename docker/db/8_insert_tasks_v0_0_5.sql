INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id) VALUES
('Init project setup', 'COMPLETED_ON_TIME', '2026-05-01 20:00:00', '2026-05-02 09:00:00', '2026-05-02 08:30:00', 0.5, 1, 1),
('DB schema design', 'COMPLETED_LATE', '2026-05-01 21:00:00', '2026-05-03 10:00:00', '2026-05-04 12:00:00', 4, 2, 1),
('Write specs', 'PENDING', '2026-05-01 23:40:00', '2026-05-04 00:20:00', NULL, 0, 0.3, 1),
('Frontend layout', 'NOT_COMPLETED', '2026-05-01 12:00:00', '2026-05-03 12:00:00', NULL, 0, 0.4, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id) VALUES
('API endpoint users', 'COMPLETED_ON_TIME', '2026-05-02 18:00:00', '2026-05-03 09:00:00', '2026-05-03 08:00:00', 0.5, 1, 1),
('Auth service', 'COMPLETED_ON_TIME', '2026-05-02 23:10:00', '2026-05-04 00:30:00', '2026-05-04 09:30:00', 0.5, 2, 1),
('Logging system', 'PENDING', '2026-05-02 11:00:00', '2026-05-05 11:00:00', NULL, 0, 1, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id) VALUES
('Cache layer', 'COMPLETED_LATE', '2026-05-03 23:50:00', '2026-05-04 00:40:00', '2026-05-05 10:00:00', 0.4, 3, 1),
('Validation rules', 'COMPLETED_ON_TIME', '2026-05-03 10:00:00', '2026-05-04 10:00:00', '2026-05-04 09:00:00', 0.5, 4, 1),
('UI fix', 'NOT_COMPLETED', '2026-05-03 11:00:00', '2026-05-05 11:00:00', NULL, 0, 4, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id) VALUES
('Refactor services', 'COMPLETED_ON_TIME', '2026-05-04 09:00:00', '2026-05-06 09:00:00', '2026-05-06 08:00:00', 0.5, 1, 1),
('DB index tuning', 'COMPLETED_LATE', '2026-05-04 00:10:00', '2026-05-06 00:40:00', '2026-05-07 11:00:00', 0.4, 2, 1),
('Report module', 'PENDING', '2026-05-04 11:00:00', '2026-05-07 11:00:00', NULL, 0, 3, 1),
('Bug hunt', 'NOT_COMPLETED', '2026-05-04 12:00:00', '2026-05-06 12:00:00', NULL, 0, 4, 1),
('Analytics', 'COMPLETED_ON_TIME', '2026-05-04 23:30:00', '2026-05-05 00:10:00', '2026-05-05 12:00:00', 0.5, 1, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id) VALUES
('Deploy service', 'COMPLETED_ON_TIME', '2026-05-05 09:00:00', '2026-05-06 09:00:00', '2026-05-06 08:45:00', 0.5, 1, 1),
('Fix NPE', 'COMPLETED_LATE', '2026-05-05 10:00:00', '2026-05-06 10:00:00', '2026-05-07 14:00:00', 0.3, 2, 1),
('Security patch', 'PENDING', '2026-05-05 11:00:00', '2026-05-07 11:00:00', NULL, 0, 4, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id) VALUES
('Monitoring setup', 'COMPLETED_ON_TIME', '2026-05-06 22:00:00', '2026-05-07 09:00:00', '2026-05-07 08:30:00', 0.5, 3, 1),
('Alerts config', 'COMPLETED_LATE', '2026-05-06 00:20:00', '2026-05-07 00:50:00', '2026-05-08 12:00:00', 0.4, 4, 1),
('Load test', 'NOT_COMPLETED', '2026-05-06 11:00:00', '2026-05-07 11:00:00', NULL, 0, 1, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id) VALUES
('Final review', 'COMPLETED_ON_TIME', '2026-05-07 09:00:00', '2026-05-08 09:00:00', '2026-05-08 08:00:00', 0.5, 1, 1),
('Cleanup logs', 'COMPLETED_LATE', '2026-05-07 10:00:00', '2026-05-08 10:00:00', '2026-05-09 12:00:00', 0.4, 2, 1),
('Sprint planning', 'PENDING', '2026-05-07 23:20:00', '2026-05-09 00:10:00', NULL, 0, 4, 1),
('Hotfix', 'NOT_COMPLETED', '2026-05-07 12:00:00', '2026-05-08 12:00:00', NULL, 0, 1, 1);

INSERT INTO tasks (title, status, created_at, due_time, completed_at, rating, user_id, project_id) VALUES
('Night feature A', 'COMPLETED_ON_TIME', '2026-05-01 22:30:00', '2026-05-02 10:00:00', '2026-05-02 09:00:00', 0.5, 4, 1),
('Night bugfix B', 'COMPLETED_LATE', '2026-05-01 23:30:00', '2026-05-03 10:00:00', '2026-05-04 12:00:00', 0.4, 4, 1),
('Late refactor', 'PENDING', '2026-05-02 23:45:00', '2026-05-04 10:00:00', NULL, 0, 4, 1),
('Midnight deploy', 'COMPLETED_ON_TIME', '2026-05-03 00:30:00', '2026-05-03 09:00:00', '2026-05-03 08:00:00', 0.5, 4, 1),
('Hotfix API', 'NOT_COMPLETED', '2026-05-03 01:15:00', '2026-05-05 10:00:00', NULL, 0, 4, 1),

('Night analytics', 'COMPLETED_ON_TIME', '2026-05-04 22:50:00', '2026-05-05 10:00:00', '2026-05-05 09:30:00', 0.5, 4, 1),
('Cache cleanup', 'COMPLETED_LATE', '2026-05-04 23:40:00', '2026-05-06 10:00:00', '2026-05-07 11:00:00', 0.4, 4, 1),
('Async processing', 'PENDING', '2026-05-05 00:20:00', '2026-05-07 10:00:00', NULL, 0, 4, 1),
('Queue tuning', 'COMPLETED_ON_TIME', '2026-05-05 01:10:00', '2026-05-06 10:00:00', '2026-05-06 09:00:00', 0.5, 4, 1),

('Night migration', 'COMPLETED_ON_TIME', '2026-05-06 22:30:00', '2026-05-07 10:00:00', '2026-05-07 09:00:00', 0.5, 4, 1),
('DB tuning', 'COMPLETED_LATE', '2026-05-06 23:30:00', '2026-05-08 10:00:00', '2026-05-09 12:00:00', 0.4, 4, 1),
('Night monitoring', 'NOT_COMPLETED', '2026-05-07 00:40:00', '2026-05-09 10:00:00', NULL, 0, 4, 1),

('Final night job', 'COMPLETED_ON_TIME', '2026-05-07 01:20:00', '2026-05-08 10:00:00', '2026-05-08 08:30:00', 0.5, 4, 1);