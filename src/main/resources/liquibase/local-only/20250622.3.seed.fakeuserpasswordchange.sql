--liquibase formatted sql
--changeset kerdogan:20250622.3.seed.fakeuserpasswordchange splitStatements:false

UPDATE user_passwords SET password_hash = '$2a$12$B6779vKrzZuSHc8yCVMsN.HxJle3BykVr/hKr.zO9Zdblm.8nY9fq'
WHERE user_id = (SELECT id FROM users WHERE username = 'admin');
