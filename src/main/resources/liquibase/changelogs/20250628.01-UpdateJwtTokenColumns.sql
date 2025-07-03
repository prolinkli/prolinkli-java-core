-- liquibase formatted sql
-- changeset kerdogan:UpdateJwtTokenColumns splitStatements:false

-- Update access_token column to TEXT
ALTER TABLE public.jwt_tokens ALTER COLUMN access_token TYPE TEXT;

-- Update refresh_token column to TEXT  
ALTER TABLE public.jwt_tokens ALTER COLUMN refresh_token TYPE TEXT; 