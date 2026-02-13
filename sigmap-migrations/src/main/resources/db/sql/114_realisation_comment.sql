-- liquibase formatted sql

-- changeset sigmap:17496719590114-1
ALTER TABLE realisation
    ADD COLUMN comment VARCHAR(2000) NULL;
