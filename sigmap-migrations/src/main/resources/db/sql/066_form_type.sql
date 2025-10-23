-- liquibase formatted sql

-- changeset sigmap:1757950267-1

ALTER TABLE dyna_form
    ADD COLUMN form_type VARCHAR(32);