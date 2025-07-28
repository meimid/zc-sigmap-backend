-- liquibase formatted sql

-- changeset sigmap:1749671959050-37
ALTER TABLE realisation
    ADD COLUMN statut VARCHAR(50);