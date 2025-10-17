-- liquibase formatted sql

-- changeset sigmap:1757950262-1

CREATE TABLE IF NOT EXISTS dac_doc_soumission(
    dac_id INT NOT NULL,
    dac_ver INT NOT NULL,
    doc_soumission_id INT NOT NULL,
    date_validate DATETIME NULL
)

-- changeset sigmap:1749671959062-2
ALTER TABLE dac_doc_soumission ADD CONSTRAINT FK_DOC_SOUMISSION_TO_DAC_DOC_SOUMISSION
    FOREIGN KEY (doc_soumission_id) REFERENCES doc_soumission (doc_soumission_id);

-- changeset sigmap:1749671959062-3
ALTER TABLE dac_doc_soumission ADD CONSTRAINT FK_DAC_TO_DAC_DOC_SOUMISSION
    FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver);