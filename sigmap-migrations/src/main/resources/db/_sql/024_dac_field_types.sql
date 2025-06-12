-- liquibase formatted sql

-- changeset db:1732196145836-31
ALTER TABLE dyna_fields MODIFY COLUMN field_type enum('INTEGER', 'TEXT', 'DECIMAL', 'DATE', 'LIST', 'TABLE', 'SYSTEM') NOT NULL;

ALTER TABLE dyna_fields_details MODIFY COLUMN field_type enum('INTEGER', 'TEXT', 'DECIMAL', 'DATE', 'LIST', 'TABLE', 'SYSTEM') NOT NULL;
