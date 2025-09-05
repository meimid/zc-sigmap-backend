-- liquibase formatted sql

-- changeset sigmap:1749671959050-58
ALTER TABLE realisation
    ADD COLUMN imputation_budgetaire_id INT DEFAULT NULL;

-- changeset sigmap:1749671959050-59
CREATE INDEX fk_realisation_imputation_budgetaire_id ON realisation(imputation_budgetaire_id);

-- changeset sigmap:1749671959050-60
ALTER TABLE realisation ADD CONSTRAINT fk_realisation_imputation_budgetaire_id FOREIGN KEY (imputation_budgetaire_id) REFERENCES ref_param (ref_param_id) ON UPDATE CASCADE ON DELETE SET NULL;