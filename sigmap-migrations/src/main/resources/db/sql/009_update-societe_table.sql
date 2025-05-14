-- liquibase formatted sql

-- changeset db:1732196145835-17

-- First, drop the constraint if it exists
ALTER TABLE societe
DROP CONSTRAINT IF EXISTS fk_categorie_soumissionnaire_id;

-- Since all data will be deleted, we can directly change the column type
ALTER TABLE societe
    MODIFY COLUMN categorie_soumissionnaire_id INTEGER NULL COMMENT 'Catégorie du soumissionnaire (par exemple PME)';

-- Add the foreign key constraint
ALTER TABLE societe
    ADD CONSTRAINT fk_categorie_soumissionnaire_id
        FOREIGN KEY (categorie_soumissionnaire_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;
