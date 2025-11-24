-- liquibase formatted sql

-- changeset sigmap:1757950266-1

ALTER TABLE dyna_form_fields
    MODIFY COLUMN type_id VARCHAR(32) NULL;

-- changeset sigmap:1757950266-2
ALTER TABLE dyna_form_fields
    ADD COLUMN IF NOT EXISTS has_total       TINYINT(1) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS is_formula      TINYINT(1) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS formula         VARCHAR(255) NULL;