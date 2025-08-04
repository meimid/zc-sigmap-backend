-- liquibase formatted sql

-- changeset sigmap:1749671959050-70

ALTER TABLE ac
    ADD COLUMN type_com ENUM('TUTELLE','INTERNE','AUTRE') DEFAULT NULL;