-- liquibase formatted sql

-- changeset sigmap:1749671959050-63
ALTER TABLE paa
    MODIFY budget_estime DECIMAL(20,3);