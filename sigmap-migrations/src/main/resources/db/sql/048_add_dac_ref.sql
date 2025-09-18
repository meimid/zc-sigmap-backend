-- liquibase formatted sql

-- changeset sigmap:1749671959050-122
ALTER TABLE dac ADD COLUMN IF NOT EXISTS ref_dac  VARCHAR(255) NULL;