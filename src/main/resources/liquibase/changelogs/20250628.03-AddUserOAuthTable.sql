-- liquibase formatted sql
-- changeset kerdogan:AddUserOAuthTable splitStatements:false

select public.create_table(
   table_name => 'user_oauth_accounts',
   columns => 'user_id BIGINT NOT NULL,
               oauth_provider VARCHAR(20) NOT NULL,
               oauth_user_id VARCHAR(255) NOT NULL,
               display_name VARCHAR(255),
               profile_picture_url TEXT,
               locale VARCHAR(10),
               ',
   foreign_keys => '[
       {
           "column": "user_id",
           "references": "users(id)",
           "if_not_exists": true,
           "on_delete": "CASCADE"
       },
       {
           "column": "oauth_provider",
           "references": "lk_user_authentication_methods(lk_authentication_method)",
           "if_not_exists": true,
           "on_delete": "RESTRICT"
       }
   ]',
   options => '{
       "schema": "public",
       "add_soft_delete": false,
       "primary_key": "user_id, oauth_provider",
       "comment": "Table for storing OAuth account information linked to users",
       "if_not_exists": true,
       "add_timestamps": true
   }'
);

