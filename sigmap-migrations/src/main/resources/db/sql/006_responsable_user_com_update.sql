
-- liquibase formatted sql

-- changeset sigmap:1749671959050-6

ALTER TABLE com
    DROP COLUMN responsable;

ALTER TABLE user_com
    ADD COLUMN responsable TINYINT(1) DEFAULT 0 ;