-- liquibase formatted sql

-- changeset sigmap:changeset sigmap:1749671959050-122
ALTER TABLE dac ADD COLUMN ref_dac VARCHAR(255) NULL;