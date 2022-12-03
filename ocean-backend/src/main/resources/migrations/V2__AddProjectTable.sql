CREATE TABLE projects (
    project_id BIGSERIAL PRIMARY KEY,
    name VARCHAR ( 64 ) UNIQUE NOT NULL,
    description VARCHAR ( 255 ) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    owner_id BIGSERIAL NOT NULL,
    CONSTRAINT fk_owner FOREIGN KEY(owner_id) REFERENCES accounts(account_id) ON DELETE RESTRICT
);
