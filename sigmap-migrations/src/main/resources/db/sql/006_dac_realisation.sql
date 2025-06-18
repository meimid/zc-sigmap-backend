-- liquibase formatted sql

-- changeset sigmap:1749671959050-6


ALTER TABLE `dac`
    ADD COLUMN `realisation_id` INT DEFAULT NULL,
    ADD COLUMN `realisation_ver` INT DEFAULT NULL;

ALTER TABLE `dac` ADD CONSTRAINT `FK_DAC_REALISATION` FOREIGN KEY (`realisation_id`, `realisation_ver`) REFERENCES `realisation` (`realisation_id`, `realisation_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;

