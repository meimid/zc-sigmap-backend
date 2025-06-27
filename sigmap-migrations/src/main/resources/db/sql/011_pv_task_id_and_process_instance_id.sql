-- liquibase formatted sql

-- changeset sigmap:1749671959050-15
ALTER Table process_verbal ADD COLUMN task_id VARCHAR(100) NULL ;
-- changeset sigmap:1749671959050-16
ALTER Table process_verbal ADD COLUMN process_instance_id VARCHAR(100) NULL ;
