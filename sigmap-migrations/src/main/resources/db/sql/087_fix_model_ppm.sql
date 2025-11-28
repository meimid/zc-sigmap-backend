-- liquibase formatted sql

-- changeset sigmap:1757950288-1
ALTER TABLE model_ppm DROP FOREIGN KEY FK_MODEL_PPM_PPM;

ALTER TABLE model_ppm DROP INDEX UQ_MODEL_PPM__PPM;

ALTER TABLE model_ppm
    ADD UNIQUE KEY UQ_MODEL_PPM__PPM (ppm_id, ppm_ver, is_deleted);

ALTER TABLE model_ppm
    ADD CONSTRAINT FK_MODEL_PPM_PPM
        FOREIGN KEY (ppm_id, ppm_ver)
            REFERENCES ppm(ppm_id, ppm_ver);

