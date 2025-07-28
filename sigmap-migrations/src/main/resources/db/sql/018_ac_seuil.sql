
-- liquibase formatted sql

-- changeset sigmap:1749671959050-44

CREATE TABLE ac_seuil ( ac_id INT NOT NULL,
                        type_marcher_id INT NOT NULL,
                        mode_passation_id INT NOT NULL,
                        seuil Decimal(10,3));

-- changeset sigmap:1749671959050-45
ALTER TABLE ac_seuil ADD CONSTRAINT FK_AC_SEUIL_TO_AC
    FOREIGN KEY (ac_id) REFERENCES ac (ac_id);

-- changeset sigmap:1749671959050-46
ALTER TABLE ac_seuil ADD CONSTRAINT FK_AC_SEUIL_TO_TYPE_MARCHER
    FOREIGN KEY (type_marcher_id) REFERENCES ref_param (ref_param_id);

-- changeset sigmap:1749671959050-47
ALTER TABLE ac_seuil ADD CONSTRAINT FK_AC_SEUIL_TO_MODE_PASSATION
    FOREIGN KEY (mode_passation_id) REFERENCES ref_param (ref_param_id);