
-- liquibase formatted sql

-- changeset sigmap:1757950270-6
ALTER TABLE dac
    ADD COLUMN short_listing TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN parent_dac_id  INT NULL;

