-- liquibase formatted sql

-- changeset db:1732196145835-14
ALTER TABLE ref_type ADD COLUMN ref_code VARCHAR(255) NOT NULL;
ALTER TABLE ref_type ADD COLUMN system_type tinyint(1) DEFAULT 0;
