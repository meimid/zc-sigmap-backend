-- liquibase formatted sql

-- changeset sigmap:1749671959050-35

INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'IMPLUMENTATION_BUDGETAIRE', 'Imputation budgétaire', false
    WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'IMPLUMENTATION_BUDGETAIRE'
);

