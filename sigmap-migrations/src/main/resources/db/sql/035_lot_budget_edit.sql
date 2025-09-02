-- liquibase formatted sql

-- changeset sigmap:1749671959050-59
ALTER TABLE lots
    MODIFY budget DECIMAL(20,3);