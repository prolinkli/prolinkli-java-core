-- liquibase formatted sql
-- changeset kerdogan:FixUserRolesConstraints splitStatements:false

-- Fix user_roles table foreign key constraint
-- Add missing foreign key constraint to users table
-- Note: user_id is already BIGINT from the initial table creation

-- First, drop any existing foreign key constraints on user_roles.user_id (if any exist)
DO $$
BEGIN
    -- Drop foreign key constraint if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name LIKE '%user_roles_user_id_fkey%'
        AND table_name = 'user_roles'
    ) THEN
        ALTER TABLE public.user_roles DROP CONSTRAINT IF EXISTS user_roles_user_id_fkey;
    END IF;
END $$;

-- Add the missing foreign key constraint
ALTER TABLE public.user_roles 
ADD CONSTRAINT user_roles_user_id_fkey 
FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

-- Add an index on user_id for better performance
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON public.user_roles(user_id); 