-- liquibase formatted sql

-- changeset sigmap:1757950287-1
ALTER TABLE model_dac DROP FOREIGN KEY fk_model_dac_dac;

ALTER TABLE model_dac DROP INDEX UQ_MODEL_DAC__DAC;

ALTER TABLE model_dac
    ADD UNIQUE KEY UQ_MODEL_DAC__DAC (dac_id, dac_ver, is_deleted);

ALTER TABLE model_dac
    ADD CONSTRAINT fk_model_dac_dac
        FOREIGN KEY (dac_id, dac_ver)
            REFERENCES dac(dac_id, dac_ver);

