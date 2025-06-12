-- liquibase formatted sql

-- changeset db:1732196145835-13

ALTER TABLE ref_param ADD COLUMN code_color VARCHAR(255) DEFAULT NULL;