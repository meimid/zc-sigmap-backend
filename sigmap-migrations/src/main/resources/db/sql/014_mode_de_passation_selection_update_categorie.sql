-- liquibase formatted sql
-- This is the new, consolidated update script.
-- It correctly sequences all the changes you want to make, including data migration.

-- changeset sigmap:1749671959050-28
-- comment: Step 1/6 - Rename the main table from 'mode_de_passation_selection' to 'mode_de_passation'.
ALTER TABLE mode_de_passation_selection RENAME TO mode_passation;

-- changeset sigmap:1749671959050-29
-- comment: Step 2/6 - Rename the primary key column to its shorter name, preserving its attributes.
ALTER TABLE mode_passation CHANGE COLUMN mode_de_passation_selection_id mode_passation_id INT AUTO_INCREMENT NOT NULL;

-- changeset sigmap:1749671959050-30
-- comment: Step 3/6 - Create the new join table using the final, correct column names.
CREATE TABLE IF NOT EXISTS mode_passation_type_marcher (
                                                           mode_passation_id INT NOT NULL,
                                                           type_marcher_id INT NOT NULL,
                                                           CONSTRAINT PK_MPTM PRIMARY KEY (mode_passation_id, type_marcher_id)
    );

-- changeset sigmap:1749671959050-31
-- comment: Step 4/6 - Add the foreign key linking the join table to the newly renamed 'mode_de_passation' table.
-- precondition-onFail: MARK_RAN
ALTER TABLE mode_passation_type_marcher ADD CONSTRAINT FK_MPTM_TO_MODE_PASSATION
    FOREIGN KEY (mode_passation_id) REFERENCES mode_passation (mode_passation_id);

-- changeset sigmap:1749671959050-32
-- comment: Step 5/6 - Add the foreign key linking the join table to the 'ref_param' table.
-- precondition-onFail: MARK_RAN
ALTER TABLE mode_passation_type_marcher ADD CONSTRAINT FK_MPTM_TO_REF_PARAM
    FOREIGN KEY (type_marcher_id) REFERENCES ref_param (ref_param_id);

-- changeset sigmap:1749671959050-33
-- comment: Step 6/7 - CRITICAL DATA MIGRATION.
INSERT INTO mode_passation_type_marcher (mode_passation_id, type_marcher_id)
SELECT mode_passation_id, categorie_id
FROM mode_de_passation
WHERE categorie_id IS NOT NULL;

-- changeset sigmap:1749671959050-34
-- comment: Step 6/6 - Final cleanup. Drop the now-redundant 'categorie_id' column after its data has been migrated.
ALTER TABLE mode_passation DROP COLUMN IF EXISTS categorie_id;
