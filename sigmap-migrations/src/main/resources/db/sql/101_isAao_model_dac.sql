-- liquibase formatted sql

-- changeset sigmap:1749671959102-1
ALTER TABLE model_dac
    ADD COLUMN is_aao TINYINT(1) NOT NULL DEFAULT 0;

-- changeset sigmap:1749671959102-2
ALTER TABLE model_dac
    DROP INDEX UQ_MODEL_DAC__DAC,
    ADD UNIQUE INDEX UQ_MODEL_DAC__DAC (dac_id, dac_ver, doc_id, is_deleted);