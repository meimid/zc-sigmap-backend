-- liquibase formatted sql

-- changeset db:1732196145835-7

-- Add  attributes do table Docs
ALTER TABLE docs
    ADD COLUMN `file_name_alfresco` VARCHAR(255) NULL,
    ADD COLUMN `path_folder` TINYTEXT NULL,
    ADD COLUMN `file_size` VARCHAR(255) NULL,
    ADD COLUMN `content_type` VARCHAR(255) NULL,
    ADD COLUMN `file_extension` VARCHAR(255) NULL;