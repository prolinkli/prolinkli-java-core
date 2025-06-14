-- liquibase formatted sql
-- changeset kerdogan:CreateTable splitStatements:false

select create_table(
	  table_name => 'lk_user_authentication_methods',
		columns => 'lk_authentication_method VARCHAR(10), 
		            description VARCHAR(255) NOT NULL, 
		            short_description VARCHAR(50) NOT NULL,',
		options => '{
			"schema": "public",
			"add_soft_delete": false,
			"primary_key": "lk_authentication_method",
			"comment": "Lookup table for user authentication methods",
			"if_not_exists": true,
			"temporary": false,
			"add_timestamps": true
		}'
);

select public.create_table(
	  table_name => 'users',
		columns => 'id BIGSERIAL UNIQUE NOT NULL,
		            username VARCHAR(50) NOT NULL UNIQUE,
								authentication_method VARCHAR(10) NOT NULL,
		            ',
		foreign_keys => '[
			{
				"column": "authentication_method",
				"references": "lk_user_authentication_methods(lk_authentication_method)",
				"if_not_exists": true,
				"on_delete": "RESTRICT"
			}
		]',
		options => '{
			"primary_key": "id",
		  "schema": "public",
			"if_not_exists": true,
			"add_soft_delete": false
		}'
);

ALTER SEQUENCE public.users_id_seq RESTART WITH 100000;

select public.create_table(
   table_name => 'user_passwords',
	 columns => 'user_id BIGINT UNIQUE NOT NULL,
							 password_hash VARCHAR(255) NOT NULL,',
	 foreign_keys => '[
			{
				"column": "user_id",
				"references": "users(id)",
				"if_not_exists": true,
				"on_delete": "RESTRICT"
			}
		]',
	 options => '{
			"schema": "public",
			"add_soft_delete": false,
			"primary_key": "user_id",
			"comment": "Table for storing user passwords",
			"if_not_exists": true,
			"add_timestamps": true
	 	}'
);
