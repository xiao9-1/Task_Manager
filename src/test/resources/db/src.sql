
CREATE TABLE public.tasks (
    id bigserial NOT NULL,
    title varchar(255) NULL,
    status varchar(50) NULL,
    created_at timestamp NULL,
    due_time timestamp NULL,
    completed_at timestamp NULL,
    rating float8 NULL,
    user_id int8 NULL,
    created_by int8 NULL,
    updated_by int8 NULL,
    updated_at timestamp NULL,
    project_id int8 NULL,
    CONSTRAINT tasks_pkey PRIMARY KEY (id)
);

CREATE TABLE public.users (
    id bigserial NOT NULL,
    "name" varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    "password" varchar(255) NOT NULL,
    "role" varchar(15) NOT NULL,
    created_at timestamp NULL,
    task_count int4 DEFAULT 0 NULL,
    is_top bool DEFAULT false NULL,
    time_zone varchar(50) NULL,
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

CREATE TABLE public.projects (
     id bigserial NOT NULL,
     "name" varchar(255) NOT NULL,
     direction_id int8 NULL,
     CONSTRAINT projects_pkey PRIMARY KEY (id)
);


CREATE TABLE public.directions (
    id bigserial NOT NULL,
    "name" varchar(255) NOT NULL,
    CONSTRAINT directions_name_key UNIQUE (name),
    CONSTRAINT directions_pkey PRIMARY KEY (id)
);

ALTER TABLE public.projects ADD CONSTRAINT fk_project_direction FOREIGN KEY (direction_id) REFERENCES public.directions(id) ON DELETE SET NULL;
ALTER TABLE public.tasks ADD CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE SET NULL;
ALTER TABLE public.tasks ADD CONSTRAINT tasks_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

