-- liquibase formatted sql
-- changeset kerdogan:AddPermissionRoleTables splitStatements:false

-- Create lk_permissions table
select create_table(
    table_name => 'lk_permissions',
    columns => 'permission_code VARCHAR(50) NOT NULL,
                permission_name VARCHAR(100) NOT NULL,
                permission_level VARCHAR(20) NOT NULL,
                permission_scope VARCHAR(50) NOT NULL,
                description VARCHAR(255),
                short_description VARCHAR(100)',
    options => '{
        "schema": "public",
        "add_soft_delete": false,
        "primary_key": "permission_code",
        "comment": "Lookup table for system permissions with levels and scopes",
        "if_not_exists": true,
        "temporary": false,
        "add_timestamps": true,
        "unique_constraints": ["permission_name"]
    }'
);

-- Create roles table
select create_table(
    table_name => 'roles',
    columns => 'id BIGSERIAL UNIQUE NOT NULL,
                role_name VARCHAR(50) NOT NULL UNIQUE,
                role_description VARCHAR(255),
                disabled_flg BOOLEAN DEFAULT FALSE,
                is_system_role BOOLEAN DEFAULT FALSE',
    options => '{
        "schema": "public",
        "add_soft_delete": false,
        "primary_key": "id",
        "comment": "Table for defining system roles",
        "if_not_exists": true,
        "temporary": false,
        "add_timestamps": true
    }'
);

-- Create role_permissions table
select create_table(
    table_name => 'role_permissions',
    columns => 'id BIGSERIAL UNIQUE NOT NULL,
                role_id BIGINT NOT NULL,
                permission_code VARCHAR(50) NOT NULL,
                disabled_flg BOOLEAN DEFAULT FALSE',
    foreign_keys => '[
        {
            "column": "role_id",
            "references": "roles(id)",
            "if_not_exists": true,
            "on_delete": "CASCADE"
        },
        {
            "column": "permission_code",
            "references": "lk_permissions(permission_code)",
            "if_not_exists": true,
            "on_delete": "CASCADE"
        }
    ]',
    options => '{
        "schema": "public",
        "add_soft_delete": false,
        "primary_key": "id",
        "comment": "Mapping table between roles and permissions",
        "if_not_exists": true,
        "temporary": false,
        "add_timestamps": true,
        "unique_constraints": ["role_id, permission_code"]
    }'
);

-- Create user_roles table
select create_table(
    table_name => 'user_roles',
    columns => 'id BIGSERIAL UNIQUE NOT NULL,
                user_id BIGINT NOT NULL,
                role_id BIGINT NOT NULL,
                assigned_by BIGINT,
                assigned_at TIMESTAMPTZ DEFAULT NOW()',
    foreign_keys => '[
        {
            "column": "user_id",
            "references": "users(id)",
            "if_not_exists": true,
            "on_delete": "CASCADE"
        },
        {
            "column": "role_id",
            "references": "roles(id)",
            "if_not_exists": true,
            "on_delete": "CASCADE"
        },
        {
            "column": "assigned_by",
            "references": "users(id)",
            "if_not_exists": true,
            "on_delete": "SET NULL"
        }
    ]',
    options => '{
        "schema": "public",
        "add_soft_delete": false,
        "primary_key": "id",
        "comment": "Mapping table between users and roles",
        "if_not_exists": true,
        "temporary": false,
        "add_timestamps": true,
        "unique_constraints": ["user_id, role_id"]
    }'
);

-- Insert basic permissions
INSERT INTO lk_permissions (permission_code, permission_name, permission_level, permission_scope, description, short_description) VALUES
('USER_READ', 'Read User', 'READ', 'USER', 'Permission to read user information', 'View user details'),
('USER_CREATE', 'Create User', 'CREATE', 'USER', 'Permission to create new users', 'Add new users'),
('USER_UPDATE', 'Update User', 'UPDATE', 'USER', 'Permission to update user information', 'Modify user details'),
('USER_DELETE', 'Delete User', 'DELETE', 'USER', 'Permission to delete users', 'Remove users'),
('SYSTEM_ADMIN', 'System Administration', 'ADMIN', 'SYSTEM', 'Full system administration access', 'System admin access');

-- Insert basic roles
INSERT INTO roles (role_name, role_description, disabled_flg, is_system_role) VALUES
('USER', 'Standard user with basic access', FALSE, TRUE),
('BUSINESS', 'Business user with elevated permissions', FALSE, TRUE),
('SYSTEM_ADMIN', 'System Administrator with full access', FALSE, TRUE); 