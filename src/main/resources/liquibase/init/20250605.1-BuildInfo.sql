--liquibase formatted sql
--changeset kerdogan:AddingBuildInfo 

CREATE TABLE IF NOT EXISTS build_info (
	version VARCHAR(20) NOT NULL,
	commit_hash VARCHAR(128) NOT NULL,
	build_date TIMESTAMP NOT NULL,
	environment VARCHAR(20) NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_buildinfo PRIMARY KEY (version, commit_hash, build_date, environment)
);
