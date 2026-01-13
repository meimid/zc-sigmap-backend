-- liquibase formatted sql

-- changeset sigmap:17496719590107-1
ALTER TABLE finacement
    ADD COLUMN examen_prealable TINYINT(1) NOT NULL DEFAULT 0;