-- liquibase formatted sql

-- changeset db:1732196145835-16

ALTER TABLE ppm ADD COLUMN process_instance_id VARCHAR(255) DEFAULT NULL;