-- liquibase formatted sql

-- changeset sigmap:1749671959050-10
CREATE SEQUENCE direction_id_seq INCREMENT BY 1 START WITH 10000;
DROP TABLE IF EXISTS ac_direction;

-- Step 1: Add the new column. We will place it physically first in the table as you want.
ALTER TABLE direction ADD COLUMN ac_id INT NOT NULL FIRST;

-- Step 2: Temporarily remove the AUTO_INCREMENT property so we can change the keys.
ALTER TABLE direction MODIFY COLUMN direction_id INT NOT NULL;

-- Step 3: Drop the old primary key.
ALTER TABLE direction DROP PRIMARY KEY;

-- Step 4: Add the new composite primary key, with the AUTO_INCREMENT column listed FIRST.
-- This is the critical step that follows the database rule.
ALTER TABLE direction ADD CONSTRAINT PK_DIRECTION_AC_COMPOSITE PRIMARY KEY (direction_id, ac_id);

-- Step 5: Now that direction_id is the first column in a key, add AUTO_INCREMENT back.
ALTER TABLE direction MODIFY COLUMN direction_id INT NOT NULL AUTO_INCREMENT;

-- Step 6: Add the foreign key constraint.
ALTER TABLE direction ADD CONSTRAINT FK_DIRECTION_AC FOREIGN KEY (ac_id) REFERENCES ac (ac_id);