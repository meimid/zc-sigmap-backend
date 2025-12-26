-- liquibase formatted sql

-- changeset sigmap:1749671959102-1
ALTER TABLE model_dac
    ADD COLUMN is_aao TINYINT(1) NOT NULL DEFAULT 0;