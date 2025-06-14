-- liquibase formatted sql 
-- changeset kerdogan:AddJwtTable splitStatements:false

select create_table(
	table_name => 'jwt_tokens',
	columns => 'user_id BIGINT NOT NULL,
							access_token VARCHAR(255) NOT NULL UNIQUE,
							refresh_token VARCHAR(255) NOT NULL UNIQUE,
							expires_at TIMESTAMP NOT NULL,
							',
	foreign_keys => '[
		{
			"column": "user_id",
			"references": "users(id)",
			"if_not_exists": true,
			"on_delete": "CASCADE"
		}
	]',
	options => '{
		"schema": "public",
		"add_soft_delete": false,
		"comment": "Table for storing JWT tokens",
		"if_not_exists": true,
		"add_timestamps": true
	}'
);
