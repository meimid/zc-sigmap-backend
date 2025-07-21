-- liquibase formatted sql

-- changeset sigmap:1749671959050-17
CREATE SEQUENCE direction_id_seq START WITH 10000 INCREMENT BY 1;

-- changeset sigmap:1749671959050-18
ALTER TABLE direction ADD COLUMN ac_id INT NULL FIRST;

-- changeset sigmap:1749671959050-19
UPDATE direction d
    JOIN (
    SELECT direction_id, MIN(ac_id) AS ac_id
    FROM ac_direction
    GROUP BY direction_id
    ) ad ON d.direction_id = ad.direction_id
    SET d.ac_id = ad.ac_id;

-- changeset sigmap:1749671959050-20
DELETE FROM direction WHERE ac_id IS NULL;

-- changeset sigmap:1749671959050-21
ALTER TABLE direction MODIFY COLUMN ac_id INT NOT NULL;

-- changeset sigmap:1749671959050-22
ALTER TABLE direction MODIFY COLUMN direction_id INT;

-- changeset sigmap:1749671959050-23
-- DROP FOREIGN KEY or DROP TABLE  ac_direction
ALTER TABLE ac_direction DROP FOREIGN KEY FK_AC_DIRECTION_DIRECTION;
-- DROP TABLE IF EXISTS ac_direction;


-- changeset sigmap:1749671959050-24
ALTER TABLE direction DROP PRIMARY KEY;

-- changeset sigmap:1749671959050-25
ALTER TABLE direction ADD CONSTRAINT PK_DIRECTION_COMPOSITE PRIMARY KEY (direction_id, ac_id);

-- changeset sigmap:1749671959050-26
ALTER TABLE direction MODIFY COLUMN direction_id INT NOT NULL AUTO_INCREMENT;

-- changeset sigmap:1749671959050-27
ALTER TABLE direction ADD CONSTRAINT FK_DIRECTION_AC FOREIGN KEY (ac_id) REFERENCES ac(ac_id);


