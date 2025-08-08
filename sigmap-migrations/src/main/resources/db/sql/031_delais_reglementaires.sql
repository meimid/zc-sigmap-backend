-- liquibase formatted sql

-- changeset sigmap:1749671959050-87

CREATE TABLE delais_reglementaires (
                             type_marche_id INT NOT NULL,
                             mode_passation_id INT NOT NULL,
                             delais_publication_lancement INT NOT NULL,
                             delais_lancement_attribution INT NOT NULL,
                             delais_attribution_demarrage INT NOT NULL,
                             created_at DATETIME NULL,
                             created_by VARCHAR(100) NULL,
                             updated_at DATETIME NULL,
                             updated_by VARCHAR(100) NULL,
                             deleted_at DATETIME NULL,
                             deleted_by VARCHAR(100) NULL,
                             is_deleted BOOLEAN DEFAULT FALSE NOT NULL
);

-- changeset sigmap:1749671959050-88
ALTER TABLE delais_reglementaires ADD CONSTRAINT fk_delais_reglementaires_type_marche_id FOREIGN KEY (type_marche_id) REFERENCES ref_param (ref_param_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-89
ALTER TABLE delais_reglementaires ADD CONSTRAINT fk_delais_reglementaires_mode_passation_id FOREIGN KEY (mode_passation_id) REFERENCES mode_passation (mode_passation_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

