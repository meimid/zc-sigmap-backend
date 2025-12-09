-- liquibase formatted sql

-- changeset sigmap:1749671959095-1
ALTER TABLE realisation
    MODIFY COLUMN realisation_envisagees VARCHAR(255) NULL;