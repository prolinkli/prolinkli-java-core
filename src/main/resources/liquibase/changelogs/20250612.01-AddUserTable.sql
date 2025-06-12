-- liquibase formatted sql
-- changeset kerdogan:CreateTable splitStatements:false

select create_table(
	  table_name => 'lk_user_authentication_methods',
		columns => 'lk_authentication_method VARCHAR(10) PRIMARY KEY, 
		            description VARCHAR(255) NOT NULL, 
		            short_description VARCHAR(50) NOT NULL',
		options => '{
			"schema": "public",
			"add_soft_delete": false,
			"primary_key": "lk_authentication_method",
			"foreign_keys": {},
			"comment": "Lookup table for user authentication methods",
			"if_not_exists": true,
			"temporary": false
			"add_timestamps": true,
		}'
)
