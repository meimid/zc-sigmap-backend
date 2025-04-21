-- liquibase formatted sql

-- changeset db:1732196145835-1

ALTER TABLE `com`
    ADD COLUMN `created_at` DATETIME(0) NULL,
    ADD COLUMN `created_by` VARCHAR(100) NULL,
    ADD COLUMN `updated_at` DATETIME(0) NULL,
    ADD COLUMN `updated_by` VARCHAR(100) NULL,
    ADD COLUMN `deleted_at` DATETIME(0) NULL,
    ADD COLUMN `deleted_by` VARCHAR(100) NULL,
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;

ALTER TABLE `societe`
    ADD COLUMN `created_at` DATETIME(0) NULL,
    ADD COLUMN `created_by` VARCHAR(100) NULL,
    ADD COLUMN `updated_at` DATETIME(0) NULL,
    ADD COLUMN `updated_by` VARCHAR(100) NULL,
    ADD COLUMN `deleted_at` DATETIME(0) NULL,
    ADD COLUMN `deleted_by` VARCHAR(100) NULL,
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;

ALTER TABLE `societe_address`
    ADD COLUMN `created_at` DATETIME(0) NULL,
    ADD COLUMN `created_by` VARCHAR(100) NULL,
    ADD COLUMN `updated_at` DATETIME(0) NULL,
    ADD COLUMN `updated_by` VARCHAR(100) NULL,
    ADD COLUMN `deleted_at` DATETIME(0) NULL,
    ADD COLUMN `deleted_by` VARCHAR(100) NULL,
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;

ALTER TABLE `compte_bancaire`
    ADD COLUMN `created_at` DATETIME(0) NULL,
    ADD COLUMN `created_by` VARCHAR(100) NULL,
    ADD COLUMN `updated_at` DATETIME(0) NULL,
    ADD COLUMN `updated_by` VARCHAR(100) NULL,
    ADD COLUMN `deleted_at` DATETIME(0) NULL,
    ADD COLUMN `deleted_by` VARCHAR(100) NULL,
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;

ALTER TABLE `societe_contact`
    ADD COLUMN `created_at` DATETIME(0) NULL,
    ADD COLUMN `created_by` VARCHAR(100) NULL,
    ADD COLUMN `updated_at` DATETIME(0) NULL,
    ADD COLUMN `updated_by` VARCHAR(100) NULL,
    ADD COLUMN `deleted_at` DATETIME(0) NULL,
    ADD COLUMN `deleted_by` VARCHAR(100) NULL,
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;

ALTER TABLE `lots`
    ADD COLUMN `created_at` DATETIME(0) NULL,
    ADD COLUMN `created_by` VARCHAR(100) NULL,
    ADD COLUMN `updated_at` DATETIME(0) NULL,
    ADD COLUMN `updated_by` VARCHAR(100) NULL,
    ADD COLUMN `deleted_at` DATETIME(0) NULL,
    ADD COLUMN `deleted_by` VARCHAR(100) NULL,
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;

CREATE TABLE `authority`
(
    `authority_code` VARCHAR(50),
    PRIMARY KEY (`authority_code`)
);

CREATE TABLE `menu_item`
(
    `id`             INT NOT NULL AUTO_INCREMENT,
    `label`          VARCHAR(100) NOT NULL,
    `icon`           VARCHAR(50),
    `route`          VARCHAR(200),
    `order_index`    INT,
    `parent_id`      INT,
    `authority_code` VARCHAR(36),
    `created_at`     DATETIME(0) NULL,
    `created_by`     VARCHAR(100) NULL,
    `updated_at`     DATETIME(0) NULL,
    `updated_by`     VARCHAR(100) NULL,
    `deleted_at`     DATETIME(0) NULL,
    `deleted_by`     VARCHAR(100) NULL,
    `is_deleted`     TINYINT(1) NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`authority_code`) REFERENCES `authority` (`authority_code`) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (`parent_id`) REFERENCES `menu_item` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- drop constraints related to societe table and societe_id column
ALTER TABLE `compte_bancaire` DROP FOREIGN KEY `fk_compte_bancaire_societe_id`;
ALTER TABLE `societe_address` DROP FOREIGN KEY `fk_societe_address _id`;
ALTER TABLE `societe_contact` DROP FOREIGN KEY `fk_societe_contact_societe_id`;

--modify societe_id column
ALTER TABLE `societe` MODIFY COLUMN `societe_id` int NOT NULL AUTO_INCREMENT;

-- recreate constraints related to societe table and societe_id column
ALTER TABLE `compte_bancaire` ADD CONSTRAINT `fk_compte_bancaire_societe_id` FOREIGN KEY (`societe_id`) REFERENCES `societe` (`societe_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `societe_address` ADD CONSTRAINT `fk_societe_address_id` FOREIGN KEY (`societe_id`) REFERENCES `societe` (`societe_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `societe_contact` ADD CONSTRAINT `fk_societe_contact_societe_id` FOREIGN KEY (`societe_id`) REFERENCES `societe` (`societe_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;


-- Modify is_deleted columns to NOT NULL with default FALSE
ALTER TABLE `societe` MODIFY COLUMN `societe_id` int NOT NULL AUTO_INCREMENT;
ALTER TABLE `ac` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;
ALTER TABLE `agpm` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;
ALTER TABLE `contact` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;
ALTER TABLE `docs` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;
ALTER TABLE `paa` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;
ALTER TABLE `ppm` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;
ALTER TABLE `realisation` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;
ALTER TABLE `ref_param` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;
ALTER TABLE `ref_type` MODIFY COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE;