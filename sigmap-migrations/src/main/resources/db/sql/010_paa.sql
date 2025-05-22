-- liquibase formatted sql

-- changeset db:1732196145835-10

ALTER TABLE paa ADD COLUMN process_instance_id VARCHAR(255) DEFAULT NULL;