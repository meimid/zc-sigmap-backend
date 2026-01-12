-- liquibase formatted sql
-- changeset sigmap:17496719590108-1
ALTER TABLE doc_soumission ADD COLUMN IF NOT EXISTS date_validation_obligatoire TINYINT(1) DEFAULT 0 NOT NULL;
