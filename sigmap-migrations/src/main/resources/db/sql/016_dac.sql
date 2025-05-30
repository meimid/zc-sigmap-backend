-- liquibase formatted sql

-- changeset db:1732196145836-1


-- Drop the existing dac table
DROP TABLE `dac`;

-- Create the updated dac table
CREATE TABLE `dac` (
       `dac_id` int NOT NULL AUTO_INCREMENT,
       `dac_ver` int NOT NULL,
       `ppm_id` int NULL,
       `ppm_ver` int NULL,
       `category_id` int NULL,
       `status_id` int NULL,
       `created_at` DATETIME(0) NULL,
       `created_by` VARCHAR(100) NULL,
       `updated_at` DATETIME(0) NULL,
       `updated_by` VARCHAR(100) NULL,
       `deleted_at` DATETIME(0) NULL,
       `deleted_by` VARCHAR(100) NULL,
       `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE,
       PRIMARY KEY (`dac_id`, `dac_ver`)
);

CREATE TABLE `dyna_docs`
(
    `dyna_docs_id`       int NOT NULL AUTO_INCREMENT,
    `dyna_docs_ver`       int NOT NULL,
    `name`               varchar(100) NULL,
    `template_file_name` varchar(100) NULL,
    `guide_file_name`  varchar(100) null,
    `category_id`      int NULL,
    `dyna_doc_order`  int NULL,
    `status`           int null,
    PRIMARY KEY (`dyna_docs_id`, `dyna_docs_ver`)

);


CREATE TABLE `dac_docs`
(
    `dac_id`         int NOT NULL,
    `dac_ver`        int NOT NULL,
    `dyna_docs_id`   int NOT NULL,
    `dyna_docs_ver`  int NOT NULL,
    `dyna_fields_id` int NOT NULL,
    `source_id`      int NOT NULL,
    `field_value`    varchar(255) NULL,
    PRIMARY KEY (`dac_id`, `dac_ver`, `dyna_docs_id`,`dyna_docs_ver`, `dyna_fields_id`)
);

CREATE TABLE `dac_docs_details`
(
    `dac_id`         int NOT NULL,
    `dac_ver`        int NOT NULL,
    `dyna_docs_id`   int NOT NULL,
    `dyna_docs_ver`  int NOT NULL,
    `dyna_fields_id` int NOT NULL,
    `dyna_fields_details_id` int NOT NULL,
    `row_id` int NOT NULL,
    `source_id`      varchar(255) NULL,
    `field_value`    varchar(255) NULL,
    PRIMARY KEY (`dac_id`, `dac_ver`, `dyna_docs_id`,`dyna_docs_ver`, `dyna_fields_id`, `dyna_fields_details_id`, `row_id`)
);

CREATE TABLE `dyna_fields`
(
    `dyna_fields_id` int NOT NULL AUTO_INCREMENT,
    `dyna_docs_id`       int NOT NULL,
    `dyna_docs_ver`       int NOT NULL,
    `name`           varchar(100) NULL,
    `ref`            varchar(100) NULL,
    `label`          varchar(100) NULL,
    `description`    varchar(255) NULL,
    `type_id`        int NULL,
    `source`         varchar(255) NULL,
    `source_field`   varchar(255) NULL,
    `dyna_fields_order`  int NULL,
    `status`             int null,
    PRIMARY KEY (`dyna_fields_id`, `dyna_docs_id`, `dyna_docs_ver`)
);


CREATE TABLE `dyna_fields_details`
(
    `dyna_fields_details_id` int NOT NULL AUTO_INCREMENT,
    `column_name`       varchar(100) NULL,
    `column_pos`        int NOT NULL,
    `type_id`           int NULL,
    `source`         varchar(255) NULL,
    `source_field`   varchar(255) NULL,
    `dyna_fields_id`  int NULL,
    PRIMARY KEY (`dyna_fields_details_id`)
);


ALTER TABLE `dac` ADD CONSTRAINT `FK_DAC_PPM` FOREIGN KEY (`ppm_id`, `ppm_ver`) REFERENCES `ppm` (`ppm_id`, `ppm_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
Alter TABLE `dac` ADD CONSTRAINT `FK_DAC_REF_PARAM` FOREIGN KEY (`category_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

Alter TABLE `dyna_docs` ADD CONSTRAINT `FK_DYNA_DOCS_REF_PARAM` FOREIGN KEY (`category_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

Alter TABLE `dyna_fields` ADD CONSTRAINT `FK_DYNA_FIELDS_REF_PARAM` FOREIGN KEY (`type_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
Alter TABLE `dyna_fields` ADD CONSTRAINT `FK_DYNA_FIELDS_DYNA_DOCS` FOREIGN KEY (`dyna_docs_id`, `dyna_docs_ver`) REFERENCES `dyna_docs` (`dyna_docs_id`, `dyna_docs_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `dyna_fields_details` ADD CONSTRAINT `FK_DYNA_FIELDS_DETAILS_REF_PARAM` FOREIGN KEY (`type_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dyna_fields_details` ADD CONSTRAINT `FK_DYNA_FIELDS_DETAILS_DYNA_FIELDS` FOREIGN KEY (`dyna_fields_id`) REFERENCES `dyna_fields` (`dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `dac_docs` ADD CONSTRAINT `FK_DAC_VER_ID` FOREIGN KEY (`dac_id`, `dac_ver`) REFERENCES `dac` (`dac_id`, `dac_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dac_docs`  ADD CONSTRAINT `FK_DYNA_DOC_FIELDS_ID` FOREIGN KEY (`dyna_docs_id`, `dyna_docs_ver`, `dyna_fields_id`) REFERENCES `dyna_fields` (`dyna_docs_id`, `dyna_docs_ver`, `dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `dac_docs_details`  ADD CONSTRAINT `FK_DAC_DOCS` FOREIGN KEY (`dyna_docs_id`, `dyna_docs_ver`, `dyna_fields_id`) REFERENCES `dyna_fields` (`dyna_docs_id`, `dyna_docs_ver`, `dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
