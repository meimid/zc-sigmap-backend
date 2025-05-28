-- liquibase formatted sql

-- changeset db:1732196145835-15
ALTER TABLE com DROP COLUMN membres;


-- changeset db:1732196145835-16
DROP TABLE IF EXISTS `com_ac`;


-- changeset db:1732196145835-17
CREATE TABLE IF NOT EXISTS `ppm_com`(
  `com_id` int NOT NULL,
  `ppm_id` int NOT NULL,
  `ppm_ver` int NOT NULL,
PRIMARY KEY (`com_id`, `ppm_id`,`ppm_ver`));

ALTER TABLE `ppm_com`
  ADD CONSTRAINT `fk_ppm_com_ppm_id` 
    FOREIGN KEY (`ppm_id`, `ppm_ver`) REFERENCES `ppm` (`ppm_id`, `ppm_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `ppm_com` 
  ADD CONSTRAINT `fk_ppm_com_com_id`
    FOREIGN KEY (`com_id`) REFERENCES `com` (`com_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;


CREATE TABLE IF NOT EXISTS `user_com` (
  `user_id` VARCHAR(100) NOT NULL,
  `com_id` int NOT NULL,
  `status_id` int NOT NULL,
  `created_at` datetime NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`user_id`, `com_id`)
);

ALTER TABLE `user_com`
    ADD CONSTRAINT fk_user_com_status_id
        FOREIGN KEY (status_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;

ALTER TABLE `user_com`
    ADD CONSTRAINT fk_user_com_com_id
        FOREIGN KEY (com_id)
            REFERENCES com (com_id)
            ON DELETE NO ACTION 
            ON UPDATE NO ACTION;


CREATE TABLE `user_ac`  (
  `user_id` VARCHAR(100) NOT NULL,
  `ac_id` int NOT NULL,
  `status_id` int NOT NULL,
  `created_at` datetime NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`user_id`, `ac_id`)
);

ALTER TABLE user_ac
    ADD CONSTRAINT fk_user_ac_status_id
        FOREIGN KEY (status_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;

ALTER TABLE user_ac
    ADD CONSTRAINT fk_user_ac_ac_id
        FOREIGN KEY (ac_id)
            REFERENCES ac (ac_id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;
 

