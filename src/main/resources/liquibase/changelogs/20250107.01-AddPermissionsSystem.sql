-- liquibase formatted sql
-- changeset cBaxendale:CreatePermissionsSystem splitStatements:false

-- Permissions lookup table
delete from permissions_lk;
select create_table(
  table_name => 'permissions_lk',
  columns => 'permission_lk VARCHAR(50) NOT NULL,
              permission_name VARCHAR(100) NOT NULL,
              has_targets_flg BOOLEAN NOT NULL DEFAULT FALSE,
              has_levels_flg BOOLEAN NOT NULL DEFAULT FALSE,
              description VARCHAR(255),',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_lk",
    "comment": "Unique permission identifier. Human readable name, flags for targets/levels, description.",
    "if_not_exists": true,
    "add_timestamps": true
  }'
);

-- Permission levels lookup table
delete from permissions_levels_lk;
select create_table(
  table_name => 'permissions_levels_lk',
  columns => 'permission_level_lk VARCHAR(15) NOT NULL,
              level_value INTEGER NOT NULL,
              level_name VARCHAR(50) NOT NULL,',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_level_lk",
    "comment": "NONE, READ, EDIT, CREATE, DELETE. Bitwise values.",
    "if_not_exists": true,
    "add_timestamps": true,
    "unique_constraints": ["level_value"]
  }'
);

-- Permission targets lookup table
delete from permissions_targets_lk;
select create_table(
  table_name => 'permissions_targets_lk',
  columns => 'permission_target_lk VARCHAR(50) NOT NULL,
              target_name VARCHAR(100) NOT NULL,',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_target_lk",
    "comment": "ALL, SELF, PROFESSIONALS, CONSUMER, etc.",
    "if_not_exists": true,
    "add_timestamps": true
  }'
);

-- Possible targets for each permission
select create_table(
  table_name => 'permissions_possible_targets_lk',
  columns => 'permission_lk VARCHAR(50) NOT NULL,
              permission_target_lk VARCHAR(50) NOT NULL,',
  foreign_keys => '[
    {
      "column": "permission_lk",
      "references": "permissions_lk(permission_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_target_lk",
      "references": "permissions_targets_lk(permission_target_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    }
  ]',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_lk, permission_target_lk",
    "comment": "Composite PK. Maps which targets are valid for each permission.",
    "if_not_exists": true,
    "add_timestamps": false
  }'
);

-- Possible levels for each permission
select create_table(
  table_name => 'permissions_possible_levels_lk',
  columns => 'permission_lk VARCHAR(50) NOT NULL,
              permission_level_lk VARCHAR(15) NOT NULL,',
  foreign_keys => '[
    {
      "column": "permission_lk",
      "references": "permissions_lk(permission_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_level_lk",
      "references": "permissions_levels_lk(permission_level_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    }
  ]',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_lk, permission_level_lk",
    "comment": "Composite PK. Maps which levels are valid for each permission.",
    "if_not_exists": true,
    "add_timestamps": false
  }'
);

-- Role permissions table
select create_table(
  table_name => 'role_permissions',
  columns => 'role_permission_id VARCHAR(36) NOT NULL DEFAULT gen_random_uuid(),
              role_id VARCHAR(50) NOT NULL,
              permission_lk VARCHAR(50) NOT NULL,
              permission_target_lk VARCHAR(50) NULL,
              permission_level_lk VARCHAR(15) NULL,
              granted_at TIMESTAMPTZ DEFAULT NOW(),
              granted_by VARCHAR(50),',
  foreign_keys => '[
    {
      "column": "role_id",
      "references": "roles(role_id)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_lk",
      "references": "permissions_lk(permission_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_target_lk",
      "references": "permissions_targets_lk(permission_target_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "permission_level_lk",
      "references": "permissions_levels_lk(permission_level_lk)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "granted_by",
      "references": "roles(role_id)",
      "if_not_exists": true,
      "on_delete": "SET NULL"
    }
  ]',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "role_permission_id",
    "comment": "Role-specific permission assignments. Unique index on (role_id, permission_lk, permission_target_lk, permission_level_lk)",
    "if_not_exists": true,
    "add_timestamps": true,
    "unique_constraints": ["role_id, permission_lk, permission_target_lk, permission_level_lk"]
  }'
);

-- Roles table
select create_table(
  table_name => 'roles',
  columns => 'role_id VARCHAR(50) NOT NULL,
              description VARCHAR(50) NOT NULL,
              short_description VARCHAR(15) NOT NULL,
              created_at TIMESTAMPTZ DEFAULT NOW(),',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "role_id",
    "comment": "Role definitions. Unique description and short_description.",
    "if_not_exists": true,
    "add_timestamps": false,
    "unique_constraints": ["description", "short_description"]
  }'
);

-- User roles table
select create_table(
  table_name => 'user_roles',
  columns => 'role_id VARCHAR(50) NOT NULL,
              user_id VARCHAR(50) NOT NULL,
              active_flg BOOLEAN NOT NULL DEFAULT TRUE,
              created_at TIMESTAMPTZ DEFAULT NOW(),',
  foreign_keys => '[
    {
      "column": "role_id",
      "references": "roles(role_id)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    }
  ]',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "role_id, user_id",
    "comment": "User to role assignments.",
    "if_not_exists": true,
    "add_timestamps": false
  }'
); 