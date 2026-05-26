CREATE TABLE directions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    direction_id BIGINT,
    CONSTRAINT fk_project_direction
        FOREIGN KEY (direction_id)
        REFERENCES directions(id)
        ON DELETE SET NULL
);

ALTER TABLE tasks
ADD CONSTRAINT fk_task_project
    FOREIGN KEY (project_id)
    REFERENCES projects(id)
    ON DELETE SET NULL;