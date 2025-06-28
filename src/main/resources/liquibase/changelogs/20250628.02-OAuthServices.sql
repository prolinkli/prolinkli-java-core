--liquibase formatted sql
--changeset kerdogan:20230628.02-OAuthServices
INSERT into "lk_user_authentication_methods" 
(lk_authentication_method, description, short_description)
VALUES 
('GOOGLE', 'Google OAuth authentication method', 'Google OAuth'),
('FACEBOOK', 'Facebook OAuth authentication method', 'Facebook OAuth'),
('GITHUB', 'GitHub OAuth authentication method', 'GitHub OAuth'),
('MICROSOFT', 'Microsoft OAuth authentication method', 'Microsoft OAuth'),
('APPLE', 'Apple OAuth authentication method', 'Apple OAuth');
