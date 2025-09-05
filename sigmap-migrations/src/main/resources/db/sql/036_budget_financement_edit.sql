-- liquibase formatted sql

-- changeset sigmap:1749671959050-59
ALTER TABLE finacement
    MODIFY budget_estimer DECIMAL(20,3);