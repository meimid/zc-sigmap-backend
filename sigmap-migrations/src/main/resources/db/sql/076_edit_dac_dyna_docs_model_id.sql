-- liquibase formatted sql

-- changeset sigmap:1757950277-1
ALTER TABLE dac ADD COLUMN IF NOT EXISTS dyna_docs_model_id INT;

-- liquibase formatted sql

-- changeset sigmap:1757950277-2
ALTER TABLE dac ADD CONSTRAINT fk_dac_dyna_docs_model
    FOREIGN KEY (dyna_docs_model_id) REFERENCES dyna_docs_model (dyna_docs_model_id);
