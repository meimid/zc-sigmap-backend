-- liquibase formatted sql

-- changeset db:1732196145835-2
-- Drop the foreign key constraint
ALTER TABLE `agpm_docs` DROP FOREIGN KEY `fk_agpm_id`;

-- Modify agpm_id to add AUTO_INCREMENT
ALTER TABLE `agpm` MODIFY COLUMN `agpm_id` int NOT NULL AUTO_INCREMENT;

-- Recreate the foreign key constraint
ALTER TABLE `agpm_docs` ADD CONSTRAINT `fk_agpm_id` FOREIGN KEY (`agpm_id`, `agpm_ver`) REFERENCES `agpm` (`agpm_id`, `agpm_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
