-- liquibase formatted sql

-- changeset sigmap:1757950257-1
ALTER TABLE dyna_docs ADD COLUMN IF NOT EXISTS mode_passation_id INT NULL;

-- changeset sigmap:1757950257-2
ALTER TABLE dyna_docs ADD CONSTRAINT fk_dyna_docs_mode_passation
    FOREIGN KEY (mode_passation_id) REFERENCES mode_passation (mode_passation_id);

-- changeset sigmap:1757950257-3
ALTER TABLE dac ADD COLUMN IF NOT EXISTS mode_passation_id INT NULL;

-- changeset sigmap:1757950257-4
ALTER TABLE dac ADD CONSTRAINT fk_dac_mode_passation
    FOREIGN KEY (mode_passation_id) REFERENCES mode_passation (mode_passation_id);