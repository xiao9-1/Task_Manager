create table users (
    id bigserial primary key,
    name varchar(255) not null,
    email varchar(255) unique not null,
    password varchar(255) not null,
    role varchar(15) not null,
    created_at timestamp,
    task_count int default 0,
    is_top boolean default false
);

create table tasks (
    id bigserial primary key,
    title varchar(255),
    status varchar(50),
    created_at timestamp,
    due_time timestamp,
    completed_at timestamp,
    rating double precision,
    user_id bigint references users(id) on delete cascade
);
