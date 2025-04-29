-- liquibase formatted sql

-- changeset db:1732196145835-3

-- Add foreign key constraint (agpm.status_id -> ref_param.ref_param_id)
ALTER TABLE agpm
    ADD CONSTRAINT fk_agpm_status_id
        FOREIGN KEY (status_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

-- Add the 'remarque' column as a VARCHAR(255), nullable
ALTER TABLE agpm ADD COLUMN remarque VARCHAR(255) DEFAULT NULL;