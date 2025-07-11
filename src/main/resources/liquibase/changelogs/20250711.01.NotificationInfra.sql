--liquibase formatted sql
--changeset kerdogan:20250711.01.NotificationInfra

SELECT public.create_table(
  table_name => 'lk_notification_channels',
  columns => 'lk_notification_channel VARCHAR(10),
              description VARCHAR(100),
              short_description VARCHAR(100),
              is_active BOOLEAN DEFAULT TRUE',
  options => '{
    "schema": "public",
    "if_not_exists": true,
    "add_soft_delete": false,
    "primary_key": "lk_notification_channel",
    "add_timestamps": true,
    "comment": "Lookup table for notification channels"
  }'
);

SELECT public.create_table(
  table_name => 'notification_templates',
  columns => 'template_name VARCHAR(50),
              template_description VARCHAR(100),
              lk_notification_channel VARCHAR(10),
              subject VARCHAR(255),
              body TEXT,
              is_active BOOLEAN DEFAULT TRUE',
  foreign_keys => '[
    {
      "column": "lk_notification_channel",
      "references": "lk_notification_channels(lk_notification_channel)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    }
  ]',
  options => '{
    "schema": "public",
    "if_not_exists": true,
    "add_soft_delete": false,
    "primary_key": "template_name",
    "add_timestamps": true,
    "comment": "Table for notification templates"
  }'
);

SELECT public.create_table(
  table_name => 'notification_providers',
  columns => 'lk_notification_channel VARCHAR(10),
              provider_name VARCHAR(25),
              provider_description VARCHAR(100),
              is_active BOOLEAN DEFAULT TRUE',
  foreign_keys => '[
    {
      "column": "lk_notification_channel",
      "references": "lk_notification_channels(lk_notification_channel)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    }
  ]',
  options => '{
    "schema": "public",
    "if_not_exists": true,
    "add_soft_delete": false,
    "primary_key": "provider_name",
    "add_timestamps": true,
    "comment": "Table for notification providers"
  }'
);

SELECT public.create_table(
  table_name => 'lk_notification_channel_rules',
  columns => 'lk_notification_channel_rule VARCHAR(15),
              rule_description VARCHAR(100),
              is_active BOOLEAN DEFAULT TRUE',
  options => '{
    "schema": "public",
    "if_not_exists": true,
    "add_soft_delete": false,
    "primary_key": "lk_notification_channel_rule",
    "add_timestamps": true,
    "comment": "Lookup table for notification channel rules"
  }'
);

SELECT public.create_table(
  table_name => 'notification_channel_rules',
  columns => 'lk_notification_channel_rule VARCHAR(15),
              provider_name VARCHAR(25),
              rule_value TEXT',
  foreign_keys => '[
    {
      "column": "lk_notification_channel_rule",
      "references": "lk_notification_channel_rules(lk_notification_channel_rule)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    },
    {
      "column": "provider_name",
      "references": "notification_providers(provider_name)",
      "if_not_exists": true,
      "on_delete": "CASCADE"
    }
  ]',
  options => '{
    "schema": "public",
    "if_not_exists": true,
    "add_soft_delete": false,
    "primary_key": "lk_notification_channel_rule, provider_name",
    "add_timestamps": true,
    "comment": "Table for notification channel rules"
  }'
);

