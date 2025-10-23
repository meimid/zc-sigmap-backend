
-- liquibase formatted sql

-- changeset sigmap:1757950270-8
ALTER TABLE dyna_docs ADD COLUMN dyna_docs_model_id INT;

-- liquibase formatted sql

-- changeset sigmap:1757950270-9
ALTER TABLE dyna_docs ADD CONSTRAINT fk_dyna_docs_model
    FOREIGN KEY (dyna_docs_model_id) REFERENCES dyna_docs_model (dyna_docs_model_id);