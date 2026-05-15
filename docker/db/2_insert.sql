insert into users (name, email, password, role, created_at, task_count, is_top)
values (
    'Admin', 
    'admin@example.com', 
    '{noop}admin',
    'ADMIN', 
    '2026-05-14 10:00:00', 
    0, 
    false
);

insert into users (name, email, password, role, created_at, task_count, is_top)
values
    ('Иван Иванов', 'ivan@example.com', '{noop}ivan123', 'USER', '2026-05-12 10:30:00', 3, true),
    ('Петр Петров', 'petr@example.com', '{noop}petr123', 'USER', '2026-05-12 11:00:00', 1, false),
    ('Анна Смирнова', 'anna@example.com', '{noop}anna123', 'USER', '2026-05-12 12:15:00', 0, false);

insert into tasks (title, status, created_at, due_time, completed_at, rating, user_id)
values
    (
        'Подготовить отчет',
        'PENDING',
        '2026-05-12 09:00:00',
        '2026-05-15 18:00:00',
        null,
        0.1,
        2
    ),
    (
        'Сделать презентацию',
        'NOT_COMPLETED',
        '2026-05-10 18:00:00',
        '2026-05-09 17:00:00',
        null,
        1.0,
        2
    ),
    (
        'Купить молоко',
        'COMPLETED_LATE',
        '2026-05-10 18:00:00',
        '2026-05-11 17:00:00',
        '2026-05-11 18:00:00',
        1.0,
        2
    );
insert into tasks (title, status, created_at, due_time, completed_at, rating, user_id)
values
    (
        'Проверить документацию',
        'COMPLETED_ON_TIME',
        '2026-05-12 08:30:00',
        '2026-05-20 12:00:00',
        '2026-05-12 10:30:00',
        0.4,
        3
);