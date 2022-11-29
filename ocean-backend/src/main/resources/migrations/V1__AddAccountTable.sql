CREATE TABLE accounts (
	account_id BIGSERIAL PRIMARY KEY,
	username VARCHAR ( 50 ) NOT NULL,
	email VARCHAR ( 255 ) UNIQUE NOT NULL,
	firstname VARCHAR ( 255 ) NOT NULL,
	lastname VARCHAR ( 255 ) NOT NULL,
	employee_type VARCHAR ( 255 ) NOT NULL,
	created_at TIMESTAMP NOT NULL,
    authenticator_type VARCHAR ( 255 ) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    password_hash VARCHAR ( 255 ),
    UNIQUE (username, authenticator_type)
);
