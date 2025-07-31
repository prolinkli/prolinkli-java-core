--liquibase formatted sql
--changeset kerdogan:20250731.02-AddUserDetailsTable


SELECT create_table(
  table_name => 'user_details',
  columns => 'user_id BIGINT NOT NULL,
              email VARCHAR(255) NOT NULL UNIQUE,
              first_name VARCHAR(100) NOT NULL,
              last_name VARCHAR(100) NOT NULL,
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
    "comment": "Table for storing user details",
    "if_not_exists": true,
    "add_timestamps": true
  }'
);
