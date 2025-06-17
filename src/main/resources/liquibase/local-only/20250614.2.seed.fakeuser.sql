--liquibase formatted sql
-- changeset kerdogan:20250614.2.seed.fakeuser splitStatements:false

INSERT into "users" (username, authentication_method) VALUES (
	'admin', 'INTERNAL'
);

INSERT INTO user_passwords (user_id, password_hash)
VALUES (
  (SELECT id FROM users WHERE username = 'admin'),
  'password'
);

