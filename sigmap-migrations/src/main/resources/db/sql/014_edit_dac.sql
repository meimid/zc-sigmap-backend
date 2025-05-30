-- liquibase formatted sql

-- changeset db:1732196145840-1

ALTER TABLE `dyna_docs_fields` DROP FOREIGN KEY `FK_DYNA_DOCS_ID`;
ALTER TABLE `dyna_docs_fields` DROP FOREIGN KEY `FK_DYNA_FIELDS_ID`;

ALTER TABLE `dac_docs` DROP FOREIGN KEY `FK_DAC_VER_ID`;
ALTER TABLE `dac_docs` DROP FOREIGN KEY `FK_DYNA_DOC_FIELDS_ID`;




------------------------------dyna_docs_fields------------------------------

-- Drop the existing dyna_docs_fields table
DROP TABLE `dyna_docs_fields`;

--re-create the dyna_docs_fields table
CREATE TABLE `dyna_docs_fields`
(
    `dyna_docs_id`   int NOT NULL,
    `dyna_docs_ver`   int NOT NULL,
    `dyna_fields_id` int NOT NULL,
    `dyna_field_order` int NULL,
    `status`         tinyint(1) NULL DEFAULT 1,
    PRIMARY KEY (`dyna_docs_id`, `dyna_docs_ver`, `dyna_fields_id`)
);

------------------------------dyna_docs------------------------------

-- Drop the existing dyna_docs table
DROP TABLE `dyna_docs`;

--re-create the dyna_docs table
CREATE TABLE `dyna_docs`
(
    `dyna_docs_id`        int NOT NULL AUTO_INCREMENT,
    `dyna_docs_ver`       int NOT NULL,
    `name`                varchar(100) NULL,
    `template_file_name`  varchar(100) NULL,
    `guide_file_name`     varchar(100) NULL,
    `category_id`            int NULL,
    `dyna_doc_order`         int NULL,
    `status`                 int NULL,
    PRIMARY KEY (`dyna_docs_id`,`dyna_docs_ver`)
);



------------------------------dyna_fields------------------------------

-- Alter the existing dyna_fields table
ALTER TABLE `dyna_fields`
    ADD COLUMN `source` VARCHAR(100) NULL,
    ADD COLUMN `source_field` VARCHAR(100) NULL;







------------------------------dac_docs------------------------------

-- Drop the existing dac_docs table
DROP TABLE `dac_docs`;

--re-create the dac_docs table
CREATE TABLE `dac_docs`
(
    `dac_id`         int NOT NULL,
    `dac_ver`        int NOT NULL,
    `dyna_docs_id`   int NOT NULL,
    `dyna_docs_ver`  int NOT NULL,
    `dyna_fields_id` int NOT NULL,
    `source_id`      int NULL,
    `field_value`    varchar(255) NULL,
    PRIMARY KEY (`dac_id`, `dac_ver`, `dyna_docs_ver`, `dyna_docs_id`, `dyna_fields_id`)
);



------------------------------dac------------------------------

-- Alter the existing dac table
ALTER TABLE `dac`
    ADD COLUMN `ac_id` int NULL,
    ADD COLUMN `category_id` int NULL,
    ADD COLUMN `status_id` int NULL;



-------------------------------dyna_fields_details------------------------------

--create the dyna_fields_details table
CREATE TABLE `dyna_fields_details`
(
    `dyna_fields_details_id`       int NOT NULL AUTO_INCREMENT,
    `column_name`                  varchar(100) NULL,
    `column_pos`                   int NULL,
    `type_id`                      int NULL,
    `source`                       varchar(100) NULL,
    `source_field`                 varchar(100) NULL,
    `dyna_fields_id`                int NULL,
    PRIMARY KEY (`dyna_fields_details_id`)
);



-------------------------------dac_docs_details------------------------------

--create the dyna_fields_details table
CREATE TABLE `dac_docs_details`
(
    `row_id`       int NOT NULL AUTO_INCREMENT,
    `dyna_fields_details_id`       int NOT NULL,
    `dac_id`                       int NOT NULL,
    `dac_ver`                      int NOT NULL,
    `dyna_docs_id`                 int NOT NULL,
    `dyna_docs_ver`                int NOT NULL,
    `dyna_fields_id`                int NOT NULL,
    `source_id`                    int NULL,
    `field_value`                  varchar(255) NULL,
    PRIMARY KEY (`row_id`, `dyna_fields_details_id`,`dac_id`, `dac_ver`, `dyna_docs_ver`, `dyna_docs_id`, `dyna_fields_id`)
);



-------------------------------Foreign keys------------------------------
ALTER TABLE `dac_docs` ADD CONSTRAINT `FK_DAC_VER_ID` FOREIGN KEY (`dac_id`, `dac_ver`) REFERENCES `dac` (`dac_id`, `dac_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dac_docs`  ADD CONSTRAINT `FK_DYNA_DOC_FIELDS_ID` FOREIGN KEY (`dyna_docs_id`, `dyna_docs_ver`, `dyna_fields_id`) REFERENCES `dyna_docs_fields` (`dyna_docs_id`, `dyna_docs_ver`, `dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dyna_docs_fields` ADD CONSTRAINT `FK_DYNA_DOCS_ID` FOREIGN KEY (`dyna_docs_id`, `dyna_docs_ver`) REFERENCES `dyna_docs` (`dyna_docs_id`, `dyna_docs_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dyna_docs_fields` ADD CONSTRAINT `FK_DYNA_FIELDS_ID` FOREIGN KEY (`dyna_fields_id`) REFERENCES `dyna_fields` (`dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dyna_fields_details` ADD CONSTRAINT `FK_DYNA_FIELDS_DETAILS_ID` FOREIGN KEY (`dyna_fields_id`) REFERENCES `dyna_fields` (`dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dac_docs_details`  ADD CONSTRAINT `FK_DAC_DOC_DETAILS` FOREIGN KEY (`dac_id`, `dac_ver`, `dyna_docs_ver`, `dyna_docs_id`, `dyna_fields_id`) REFERENCES `dac_docs` (`dac_id`, `dac_ver`, `dyna_docs_ver`, `dyna_docs_id`, `dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
Alter TABLE `dac` ADD CONSTRAINT `FK_DAC_AC` FOREIGN KEY (`ac_id`) REFERENCES `ac` (`ac_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
Alter TABLE `dac` ADD CONSTRAINT `FK_DAC_REF_PARAM` FOREIGN KEY (`category_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
Alter TABLE `dyna_docs` ADD CONSTRAINT `FK_DYNA_DOCS_REF_PARAM` FOREIGN KEY (`category_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
Alter TABLE `dyna_fields` ADD CONSTRAINT `FK_DYNA_FIELDS_REF_PARAM` FOREIGN KEY (`type_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;