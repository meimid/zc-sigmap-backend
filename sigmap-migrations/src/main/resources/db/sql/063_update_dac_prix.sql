
-- liquibase formatted sql

-- changeset sigmap:1757950261-4

ALTER TABLE dac_prix
    ADD COLUMN created_at datetime DEFAULT NULL NULL,
    ADD COLUMN created_by VARCHAR(100) NULL,
    ADD COLUMN updated_at datetime DEFAULT NULL NULL,
    ADD COLUMN updated_by VARCHAR(100) NULL,
    ADD COLUMN deleted_at datetime DEFAULT NULL NULL,
    ADD COLUMN deleted_by VARCHAR(100) NULL,
    ADD COLUMN is_deleted TINYINT(1) DEFAULT 0 NOT NULL
