-- liquibase formatted sql

-- changeset db:1732196145835-4

-- Add foreign key constraints for Realisation reference parameters
ALTER TABLE realisation
    DROP CONSTRAINT IF EXISTS fk_realisation_service_id,
    DROP CONSTRAINT IF EXISTS fk_realisation_direction_id,
    DROP CONSTRAINT IF EXISTS fk_realisation_source_financement_id,
    DROP CONSTRAINT IF EXISTS fk_realisation_monais_id,
    DROP CONSTRAINT IF EXISTS fk_realisation_type_marcher_id,
    DROP CONSTRAINT IF EXISTS fk_realisation_mode_selection_id;

ALTER TABLE realisation
    ADD CONSTRAINT fk_realisation_service_id
        FOREIGN KEY (service_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

ALTER TABLE realisation
    ADD CONSTRAINT fk_realisation_direction_id
        FOREIGN KEY (direction_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

ALTER TABLE realisation
    ADD CONSTRAINT fk_realisation_source_financement_id
        FOREIGN KEY (source_financement_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

ALTER TABLE realisation
    ADD CONSTRAINT fk_realisation_monais_id
        FOREIGN KEY (monais_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

ALTER TABLE realisation
    ADD CONSTRAINT fk_realisation_type_marcher_id
        FOREIGN KEY (type_marcher_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

ALTER TABLE realisation
    ADD CONSTRAINT fk_realisation_mode_selection_id
        FOREIGN KEY (mode_selection_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

-- Add foreign key constraint (ppm.status_id -> ref_param.ref_param_id)
ALTER TABLE ppm
    DROP CONSTRAINT IF EXISTS fk_ppm_status_id;

ALTER TABLE ppm
    ADD CONSTRAINT fk_ppm_status_id
        FOREIGN KEY (status_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

-- Add foreign key constraint (paa.status_id -> ref_param.ref_param_id)
ALTER TABLE paa
    DROP CONSTRAINT IF EXISTS fk_paa_status_id;

ALTER TABLE paa
    ADD CONSTRAINT fk_paa_status_id
        FOREIGN KEY (status_id)
            REFERENCES ref_param (ref_param_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE; 