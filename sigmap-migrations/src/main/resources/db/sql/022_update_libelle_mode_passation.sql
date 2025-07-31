-- liquibase formatted sql
-- changeset sigmap:1749671959050-58

ALTER TABLE mode_de_passation MODIFY COLUMN libelle VARCHAR(255) NOT NULL;
