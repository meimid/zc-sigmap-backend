
-- liquibase formatted sql

-- changeset sigmap:1749671959050-10

ALTER TABLE ac
    ADD COLUMN email VARCHAR(255) NULL ;
