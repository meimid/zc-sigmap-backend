-- liquibase formatted sql

-- changeset sigmap:1749671959050-5
ALTER TABLE agpm ADD COLUMN annee VARCHAR(4) NULL;
