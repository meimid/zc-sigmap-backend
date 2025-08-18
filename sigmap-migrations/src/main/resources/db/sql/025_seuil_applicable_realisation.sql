-- liquibase formatted sql

-- changeset sigmap:1749671959050-65
ALTER TABLE realisation
    ADD COLUMN seuil_applicable DECIMAL(10,3) DEFAULT NULL;