-- liquibase formatted sql

-- changeset sigmap:17496719590103-1
ALTER TABLE dac_docs
    MODIFY COLUMN field_value TEXT NULL;