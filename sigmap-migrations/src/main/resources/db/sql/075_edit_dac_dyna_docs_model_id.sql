-- liquibase formatted sql

-- changeset sigmap:1757950270-10
ALTER TABLE dac ADD COLUMN dyna_docs_model_id INT;

-- liquibase formatted sql

-- changeset sigmap:1757950270-11
ALTER TABLE dac ADD CONSTRAINT fk_dac_dyna_docs_model
    FOREIGN KEY (dyna_docs_model_id) REFERENCES dyna_docs_model (dyna_docs_model_id);
