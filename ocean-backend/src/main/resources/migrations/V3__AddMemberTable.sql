CREATE TABLE members (
    member_id BIGSERIAL PRIMARY KEY,
    role_type VARCHAR ( 64 ) NOT NULL,
    state VARCHAR ( 64 ) NOT NULL,
    project_id BIGSERIAL NOT NULL,
    account_id BIGSERIAL NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_project FOREIGN KEY(project_id) REFERENCES projects(project_id) ON DELETE RESTRICT,
    CONSTRAINT fk_account FOREIGN KEY(account_id) REFERENCES accounts(account_id) ON DELETE RESTRICT,
    UNIQUE (project_id, account_id)
);
