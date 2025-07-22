-- liquibase formatted sql

-- changeset sigmap:1749671959050-28
-- comment: Step 1/6 - Drop the unused 'categorie_id' column from the original table.
ALTER TABLE mode_de_passation_selection DROP COLUMN IF EXISTS categorie_id;

-- changeset sigmap:1749671959050-29
-- comment: Step 2/6 - Rename the main table from 'mode_de_passation_selection' to 'mode_de_passation'.
-- This is done before creating other tables that depend on it.
ALTER TABLE mode_de_passation_selection RENAME TO mode_passation;

-- changeset sigmap:1749671959050-30
-- comment: Step 3/6 - Rename the primary key column to its shorter name.
-- Using 'CHANGE COLUMN' preserves its attributes like AUTO_INCREMENT.
ALTER TABLE mode_passation CHANGE COLUMN mode_de_passation_selection_id mode_passation_id INT AUTO_INCREMENT NOT NULL;

-- changeset sigmap:1749671959050-31
-- comment: Step 4/6 - Create the new join table using the final, correct column names.
CREATE TABLE IF NOT EXISTS mode_passation_type_marcher (
                                                           mode_passation_id INT NOT NULL,
                                                           type_marcher_id INT NOT NULL,
                                                           CONSTRAINT PK_MPTM PRIMARY KEY (mode_passation_id, type_marcher_id)
    );

-- changeset sigmap:1749671959050-32
-- comment: Step 5/6 - Add the foreign key linking the join table to the newly renamed 'mode_passation' table.
-- precondition-onFail: MARK_RAN
ALTER TABLE mode_passation_type_marcher ADD CONSTRAINT FK_MPTM_TO_MODE_PASSATION
    FOREIGN KEY (mode_passation_id) REFERENCES mode_passation (mode_passation_id);

-- changeset sigmap:1749671959050-33
-- comment: Step 6/6 - Add the foreign key linking the join table to the 'ref_param' table.
-- precondition-onFail: MARK_RAN
ALTER TABLE mode_passation_type_marcher ADD CONSTRAINT FK_MPTM_TO_REF_PARAM
    FOREIGN KEY (type_marcher_id) REFERENCES ref_param (ref_param_id);



