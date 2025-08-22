-- liquibase formatted sql

-- changeset sigmap:1749671959050-62
ALTER TABLE ppm
    MODIFY budget_estime DECIMAL(20,3);