CREATE TABLE board_columns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    position INTEGER NOT NULL,
    project_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uk_board_columns_project_position UNIQUE (project_id, position)
);

CREATE INDEX idx_board_columns_project ON board_columns(project_id);

