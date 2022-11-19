CREATE TYPE AuthenticatorType AS ENUM ('directory', 'credentials');

CREATE TABLE accounts (
	account_id BIGSERIAL PRIMARY KEY,
	username VARCHAR ( 50 ) NOT NULL,
	email VARCHAR ( 255 ) UNIQUE NOT NULL,
	firstname VARCHAR ( 255 ) NOT NULL,
	lastname VARCHAR ( 255 ) NOT NULL,
	employee_type VARCHAR ( 255 ) NOT NULL,
	created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP,
    expires_at TIMESTAMP,
    authenticator_type AuthenticatorType,
    UNIQUE (username, authenticator_type)
);
