-- liquibase formatted sql

-- changeset sigmap:1749671959050-18

ALTER TABLE mode_de_passation_selection DROP COLUMN categorie_id;

-- changeset sigmap:1749671959050-19
CREATE TABLE mode_passation_categorie (
                                          mode_de_passation_selection_id INT NOT NULL,
                                          categorie_id INT NOT NULL,
                                          CONSTRAINT PK_MODE_PASSATION_CATEGORIE PRIMARY KEY (mode_de_passation_selection_id, categorie_id)
);