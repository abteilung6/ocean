CREATE TABLE accounts
(
    account_id         BIGSERIAL PRIMARY KEY,
    email              VARCHAR(255) UNIQUE NOT NULL,
    firstname          VARCHAR(255)        NOT NULL,
    lastname           VARCHAR(255)        NOT NULL,
    company            VARCHAR(255)        NOT NULL,
    created_at         TIMESTAMP           NOT NULL,
    authenticator_type VARCHAR(255)        NOT NULL,
    verified           BOOLEAN             NOT NULL DEFAULT FALSE,
    password_hash      VARCHAR(255)
);
