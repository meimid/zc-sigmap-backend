-- liquibase formatted sql
-- changeset sigmap:1749671959050-64

ALTER TABLE mode_passation MODIFY COLUMN libelle VARCHAR(255) NOT NULL;
