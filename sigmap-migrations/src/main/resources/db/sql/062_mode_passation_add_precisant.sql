-- liquibase formatted sql

-- changeset sigmap:1757950263-1

ALTER TABLE mode_passation ADD COLUMN is_restraint TINYINT(1) NOT NULL DEFAULT 0;