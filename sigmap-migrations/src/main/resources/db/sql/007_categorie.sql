-- liquibase formatted sql

-- changeset db:1732196145837-1

CREATE TABLE `categorie`
(
    `categorie_id`       int NOT NULL AUTO_INCREMENT,
    `label`               varchar(100) NULL,
    `created_at` datetime(0) NULL,
    `created_by` varchar(100) NULL,
    `updated_at` datetime(0) NULL,
    `updated_by` varchar(100) NULL,
    `deleted_at` datetime(0) NULL,
    `deleted_by` varchar(100) NULL,
    `is_deleted` tinyint(1) NULL,
    PRIMARY KEY (`categorie_id`)
);

ALTER TABLE `dyna_docs`
    ADD COLUMN `exported_type` VARCHAR(100) NULL,
    ADD COLUMN `active` tinyint(1) NULL;