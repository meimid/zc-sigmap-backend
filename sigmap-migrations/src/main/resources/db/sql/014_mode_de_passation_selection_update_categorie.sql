-- liquibase formatted sql

-- changeset sigmap:1749671959050-28
-- comment: Creates the join table, but only if it doesn't already exist.
CREATE TABLE IF NOT EXISTS mode_passation_type_marcher (
                                                        mode_de_passation_selection_id INT NOT NULL,
                                                        type_marcher_id INT NOT NULL,
                                                        CONSTRAINT MARCHER PRIMARY KEY (mode_de_passation_selection_id, type_marcher_id)
    );

-- changeset sigmap:1749671959050-29
-- comment: Drops the old 'categorie_id' column, but only if it still exists.
ALTER TABLE mode_de_passation_selection DROP COLUMN IF EXISTS categorie_id;

-- changeset sigmap:1749671959050-30
-- comment: Adds the foreign key to mode_de_passation_selection. If it already exists, this step will be marked as run and skipped.
-- precondition-onFail: MARK_RAN
ALTER TABLE mode_passation_type_marcher ADD CONSTRAINT FK_MPC_TO_MODE_PASSATION
    FOREIGN KEY (mode_de_passation_selection_id) REFERENCES mode_de_passation_selection (mode_de_passation_selection_id);

-- changeset sigmap:1749671959050-31
-- comment: Adds the foreign key to the ref_param table. If it already exists, this step will be marked as run and skipped.
-- precondition-onFail: MARK_RAN
ALTER TABLE mode_passation_type_marcher ADD CONSTRAINT FK_MPC_TO_REF_PARAM
    FOREIGN KEY (type_marcher_id) REFERENCES ref_param (ref_param_id);

