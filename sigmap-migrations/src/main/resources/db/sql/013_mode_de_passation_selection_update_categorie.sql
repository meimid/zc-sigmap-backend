-- liquibase formatted sql

-- changeset sigmap:1749671959050-100
-- comment: Creates the join table, but only if it doesn't already exist.
CREATE TABLE IF NOT EXISTS mode_passation_categorie (
                                                        mode_de_passation_selection_id INT NOT NULL,
                                                        categorie_id INT NOT NULL,
                                                        CONSTRAINT PK_MODE_PASSATION_CATEGORIE PRIMARY KEY (mode_de_passation_selection_id, categorie_id)
    );

-- changeset sigmap:1749671959050-101
-- comment: Drops the old 'categorie_id' column, but only if it still exists.
ALTER TABLE mode_de_passation_selection DROP COLUMN IF EXISTS categorie_id;

-- changeset sigmap:1749671959050-102
-- comment: Adds the foreign key to mode_de_passation_selection. If it already exists, this step will be marked as run and skipped.
-- precondition-onFail: MARK_RAN
ALTER TABLE mode_passation_categorie ADD CONSTRAINT FK_MPC_TO_MODE_PASSATION
    FOREIGN KEY (mode_de_passation_selection_id) REFERENCES mode_de_passation_selection (mode_de_passation_selection_id);

-- changeset sigmap:1749671959050-103
-- comment: Adds the foreign key to the ref_param table. If it already exists, this step will be marked as run and skipped.
-- precondition-onFail: MARK_RAN
ALTER TABLE mode_passation_categorie ADD CONSTRAINT FK_MPC_TO_REF_PARAM
    FOREIGN KEY (categorie_id) REFERENCES ref_param (ref_param_id);

