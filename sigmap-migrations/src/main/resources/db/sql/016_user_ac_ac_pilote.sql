-- liquibase formatted sql

-- changeset db:1732196145835-15
ALTER TABLE user_ac ADD COLUMN ac_pilote tinyint(1) DEFAULT 0 NOT NULL;

ALTER TABLE ref_type MODIFY COLUMN system_type TINYINT(1) NOT NULL;

