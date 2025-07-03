-- liquibase formatted sql
-- changeset kerdogan:UpdateUserCascadeDeletes splitStatements:false

-- Drop the existing foreign key constraint on user_passwords table
ALTER TABLE public.user_passwords 
DROP CONSTRAINT IF EXISTS user_passwords_user_id_fkey;

-- Add the new foreign key constraint with CASCADE on delete
ALTER TABLE public.user_passwords 
ADD CONSTRAINT user_passwords_user_id_fkey 
FOREIGN KEY (user_id) 
REFERENCES public.users(id) 
ON DELETE CASCADE;

-- Note: user_oauth_accounts table already has CASCADE on delete from the original script
-- No changes needed for that table 