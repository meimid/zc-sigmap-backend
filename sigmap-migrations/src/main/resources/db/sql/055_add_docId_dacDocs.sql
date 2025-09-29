-- liquibase formatted sql

-- changeset sigmap:1757950255-1
ALTER TABLE dac_docs ADD COLUMN IF NOT EXISTS doc_id INT NULL;

-- changeset sigmap:1757950255-2
ALTER TABLE dac_docs ADD CONSTRAINT fk_dac_docs_docId
    FOREIGN KEY (doc_id) REFERENCES docs (doc_id);

-- changeset sigmap:1757950255-3
ALTER TABLE dyna_docs ADD COLUMN IF NOT EXISTS is_static TINYINT(1) DEFAULT 0 NOT NULL;