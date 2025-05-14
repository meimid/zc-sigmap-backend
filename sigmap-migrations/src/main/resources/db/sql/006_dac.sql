-- liquibase formatted sql

-- changeset db:1732196145836-1

CREATE TABLE `dac_docs`
(
    `dac_id`         int NOT NULL,
    `dac_ver`        int NOT NULL,
    `dyna_docs_id`   int NOT NULL,
    `dyna_fields_id` int NOT NULL,
    `field_value`    varchar(255) NULL,
    PRIMARY KEY (`dac_id`, `dac_ver`, `dyna_docs_id`, `dyna_fields_id`)
);

CREATE TABLE `dyna_docs`
(
    `dyna_docs_id`       int NOT NULL AUTO_INCREMENT,
    `name`               varchar(100) NULL,
    `template_file_name` varchar(100) NULL,
    `category_id`            int NULL,
    PRIMARY KEY (`dyna_docs_id`)
);

CREATE TABLE `dyna_docs_fields`
(
    `dyna_docs_id`   int NOT NULL,
    `dyna_fields_id` int NOT NULL,
    `status`         tinyint(1) NULL DEFAULT 1,
    PRIMARY KEY (`dyna_docs_id`, `dyna_fields_id`)
);

CREATE TABLE `dyna_fields`
(
    `dyna_fields_id` int NOT NULL AUTO_INCREMENT,
    `name`           varchar(100) NULL,
    `ref`            varchar(100) NULL,
    `label`          varchar(100) NULL,
    `description`    varchar(255) NULL,
    `type_id`        int NULL,
    PRIMARY KEY (`dyna_fields_id`)
);

ALTER TABLE `dac_docs` ADD CONSTRAINT `FK_DAC_VER_ID` FOREIGN KEY (`dac_id`, `dac_ver`) REFERENCES `dac` (`dac_id`, `dac_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dac_docs`  ADD CONSTRAINT `FK_DYNA_DOC_FIELDS_ID` FOREIGN KEY (`dyna_docs_id`, `dyna_fields_id`) REFERENCES `dyna_docs_fields` (`dyna_docs_id`, `dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dyna_docs_fields` ADD CONSTRAINT `FK_DYNA_DOCS_ID` FOREIGN KEY (`dyna_docs_id`) REFERENCES `dyna_docs` (`dyna_docs_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dyna_docs_fields` ADD CONSTRAINT `FK_DYNA_FIELDS_ID` FOREIGN KEY (`dyna_fields_id`) REFERENCES `dyna_fields` (`dyna_fields_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
