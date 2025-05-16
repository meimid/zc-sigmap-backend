-- liquibase formatted sql

-- changeset db:1732196145835-4

ALTER TABLE agpm ADD COLUMN process_instance_id VARCHAR(255) DEFAULT NULL;


