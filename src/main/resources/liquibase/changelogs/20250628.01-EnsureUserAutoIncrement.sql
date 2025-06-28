-- liquibase formatted sql
-- changeset kerdogan:EnsureUserAutoIncrement splitStatements:false

-- Ensure the users table id column is properly configured for auto-increment
-- This changelog ensures the BIGSERIAL is working correctly

-- Drop and recreate the sequence to ensure it's properly configured
DROP SEQUENCE IF EXISTS public.users_id_seq CASCADE;

-- Create the sequence with proper configuration
CREATE SEQUENCE public.users_id_seq
    INCREMENT 1
    START 100000
    MINVALUE 100000
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Ensure the id column is properly linked to the sequence
ALTER TABLE public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);

-- Set the sequence to be owned by the id column
ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;

-- Grant necessary permissions
GRANT USAGE, SELECT ON SEQUENCE public.users_id_seq TO public; 