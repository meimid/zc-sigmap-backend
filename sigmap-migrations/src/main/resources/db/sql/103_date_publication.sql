-- liquibase formatted sql
-- changeset sigmap:17496719590104-1

ALTER TABLE agpm
    ADD COLUMN published_at DATETIME NULL;

ALTER TABLE dac
    ADD COLUMN published_at DATETIME NULL;

ALTER TABLE paa
    ADD COLUMN published_at DATETIME NULL;

ALTER TABLE ppm
    ADD COLUMN published_at DATETIME NULL;

