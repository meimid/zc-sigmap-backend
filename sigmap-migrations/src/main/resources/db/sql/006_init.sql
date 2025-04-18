-- liquibase formatted sql

-- changeset db:1732196145840-1

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
    `created_at`     datetime(0) NULL,
    `created_by`     varchar(100) NULL,
    `updated_at`     datetime(0) NULL,
    `updated_by`     varchar(100) NULL,
    `deleted_at`     datetime(0) NULL,
    `deleted_by`     varchar(100) NULL,
    `is_deleted`     tinyint(1) NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`authority_code`) REFERENCES `authority` (`authority_code`) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (`parent_id`) REFERENCES `menu_item` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);