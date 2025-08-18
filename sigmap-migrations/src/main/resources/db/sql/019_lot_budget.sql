-- liquibase formatted sql

-- changeset sigmap:1749671959050-36
ALTER TABLE lots
    ADD COLUMN budget DECIMAL(10,2);