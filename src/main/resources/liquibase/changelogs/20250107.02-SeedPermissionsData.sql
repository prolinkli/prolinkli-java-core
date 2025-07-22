-- liquibase formatted sql
-- changeset cBaxendale:SeedPermissionsData splitStatements:false

-- Insert permission levels with bitwise values
INSERT INTO permissions_levels_lk (permission_level_lk, level_value, level_name) VALUES
('NONE', 0, 'No Access'),
('READ', 1, 'Read Only'),
('EDIT', 3, 'Edit (includes Read)'),
('CREATE', 7, 'Create (includes Read, Edit)'),
('DELETE', 15, 'Delete (includes Read, Edit, Create)');

-- Insert permission targets
INSERT INTO permissions_targets_lk (permission_target_lk, target_name) VALUES
('ALL', 'All Resources'),
('SELF', 'Own Resources Only'),
('PROFESSIONALS', 'Professional Users'),
('CONSUMERS', 'Consumer Users'),
('TEAM', 'Team Members'),
('ORGANIZATION', 'Organization Members');

-- Insert sample permissions
INSERT INTO permissions_lk (permission_lk, permission_name, has_targets_flg, has_levels_flg, description) VALUES
('QUOTE', 'Quote Management', true, true, 'Manage quotes and pricing'),
('USER', 'User Management', true, true, 'Manage user accounts'),
('REPORT', 'Report Access', true, true, 'Access and generate reports'),
('ADMIN', 'Admin Panel', false, false, 'Administrative access'),
('PROFILE', 'Profile Management', false, true, 'Manage user profiles');

-- Map valid targets for each permission
INSERT INTO permissions_possible_targets_lk (permission_lk, permission_target_lk) VALUES
('QUOTE', 'ALL'),
('QUOTE', 'SELF'),
('QUOTE', 'PROFESSIONALS'),
('USER', 'ALL'),
('USER', 'TEAM'),
('USER', 'ORGANIZATION'),
('REPORT', 'ALL'),
('REPORT', 'TEAM'),
('PROFILE', 'SELF');

-- Map valid levels for each permission
INSERT INTO permissions_possible_levels_lk (permission_lk, permission_level_lk) VALUES
('QUOTE', 'READ'),
('QUOTE', 'EDIT'),
('QUOTE', 'CREATE'),
('QUOTE', 'DELETE'),
('USER', 'READ'),
('USER', 'EDIT'),
('USER', 'CREATE'),
('USER', 'DELETE'),
('REPORT', 'READ'),
('REPORT', 'CREATE'),
('PROFILE', 'READ'),
('PROFILE', 'EDIT'); 