-- liquibase formatted sql

-- changeset db:1732196145835-1

ALTER TABLE `com`
    ADD COLUMN `created_at` DATETIME(0) NULL,
ADD COLUMN `created_by` VARCHAR(100) NULL,
ADD COLUMN `updated_at` DATETIME(0) NULL,
ADD COLUMN `updated_by` VARCHAR(100) NULL,
ADD COLUMN `deleted_at` DATETIME(0) NULL,
ADD COLUMN `deleted_by` VARCHAR(100) NULL,
ADD COLUMN `is_deleted` TINYINT(1) NULL;

