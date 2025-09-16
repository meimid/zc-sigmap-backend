-- liquibase formatted sql

-- changeset sigmap:1749671959050-123
ALTER Table dac ADD COLUMN IF NOT EXISTS process_instance_id  VARCHAR(255) NULL ;
