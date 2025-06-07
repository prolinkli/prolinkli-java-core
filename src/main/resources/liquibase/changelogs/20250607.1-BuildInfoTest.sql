--liquibase formatted sql
--changeset kerdogan:BuildInfoTest 

INSERT INTO build_info (version, commit_hash, build_date, environment)
VALUES ('1.0.0', 'no-hash', '2025-06-07', 'dev');
