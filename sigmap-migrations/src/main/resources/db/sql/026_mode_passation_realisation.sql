-- liquibase formatted sql

-- changeset sigmap:1749671959050-66
ALTER TABLE realisation
    ADD COLUMN mode_passation_id INT DEFAULT NULL;

-- changeset sigmap:1749671959050-67
CREATE INDEX fk_realisation_mode_passation_id ON realisation(mode_passation_id);

-- changeset sigmap:1749671959050-68
ALTER TABLE realisation
    ADD CONSTRAINT fk_realisation_mode_passation_id
        FOREIGN KEY (mode_passation_id) REFERENCES mode_passation(mode_passation_id) ON UPDATE CASCADE ON DELETE SET NULL;
