-- liquibase formatted sql

-- changeset sigmap:1749671959050-60
ALTER TABLE realisation
    MODIFY seuil_applicable DECIMAL(20,3);

-- changeset sigmap:1749671959050-61
ALTER TABLE realisation
    MODIFY budget_estime DECIMAL(20,3);
