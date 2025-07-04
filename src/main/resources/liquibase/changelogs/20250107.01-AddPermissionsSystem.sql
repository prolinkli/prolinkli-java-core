-- liquibase formatted sql
-- changeset permissions:CreatePermissionsSystem splitStatements:false

-- Permissions lookup table
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
    "comment": "Lookup table for permission types",
    "if_not_exists": true,
    "add_timestamps": true
  }'
);

-- Permission levels lookup table
select create_table(
  table_name => 'permissions_levels_lk',
  columns => 'permission_level_lk VARCHAR(15) NOT NULL,
              level_value INTEGER NOT NULL,
              level_name VARCHAR(50) NOT NULL,',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_level_lk",
    "comment": "Lookup table for permission levels with bitwise values",
    "if_not_exists": true,
    "add_timestamps": true,
    "unique_constraints": ["level_value"]
  }'
);

-- Permission targets lookup table
select create_table(
  table_name => 'permissions_targets_lk',
  columns => 'permission_target_lk VARCHAR(50) NOT NULL,
              target_name VARCHAR(100) NOT NULL,',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "permission_target_lk",
    "comment": "Lookup table for permission targets",
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
    "comment": "Maps which targets are valid for each permission",
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
    "comment": "Maps which levels are valid for each permission",
    "if_not_exists": true,
    "add_timestamps": false
  }'
);

-- User permissions table
select create_table(
  table_name => 'user_permissions',
  columns => 'user_permission_id VARCHAR(36) NOT NULL DEFAULT gen_random_uuid(),
              user_id BIGINT NOT NULL,
              permission_lk VARCHAR(50) NOT NULL,
              permission_target_lk VARCHAR(50) NULL,
              permission_level_lk VARCHAR(15) NULL,
              granted_at TIMESTAMPTZ DEFAULT NOW(),
              granted_by BIGINT,',
  foreign_keys => '[
    {
      "column": "user_id",
      "references": "users(id)",
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
      "references": "users(id)",
      "if_not_exists": true,
      "on_delete": "SET NULL"
    }
  ]',
  options => '{
    "schema": "public",
    "add_soft_delete": false,
    "primary_key": "user_permission_id",
    "comment": "User-specific permission assignments",
    "if_not_exists": true,
    "add_timestamps": true,
    "unique_constraints": ["user_id, permission_lk, permission_target_lk, permission_level_lk"]
  }'
); 