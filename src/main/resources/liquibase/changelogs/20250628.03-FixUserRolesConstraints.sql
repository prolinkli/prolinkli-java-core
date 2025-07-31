-- liquibase formatted sql
-- changeset kerdogan:FixUserRolesConstraints splitStatements:false

-- Add index on user_roles.user_id for better performance
-- Note: Foreign key constraint is already defined in the table creation

-- Add an index on user_id for better performance
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON public.user_roles(user_id); 