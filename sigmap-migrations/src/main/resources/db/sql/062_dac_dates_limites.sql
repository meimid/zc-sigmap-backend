
-- liquibase formatted sql

-- changeset sigmap:1757950261-3
ALTER TABLE dac
    ADD COLUMN date_limite_soumission datetime DEFAULT NULL NULL,
    ADD COLUMN date_limite_recption_demande datetime DEFAULT NULL NULL,
    ADD COLUMN payant TINYINT(1) DEFAULT 0 NOT NULL