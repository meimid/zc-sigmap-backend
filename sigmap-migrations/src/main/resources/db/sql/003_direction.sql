-- liquibase formatted sql

-- changeset sigmap:1749671959050-3
CREATE TABLE direction (direction_id INT AUTO_INCREMENT NOT NULL, libelle VARCHAR(20) NOT NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_DIRECTION PRIMARY KEY (direction_id));
-- This command links the join table back to the 'ac' table.
CREATE TABLE ac_direction (
                              ac_id INT NOT NULL,
                              direction_id INT NOT NULL,

                              CONSTRAINT PK_AC_DIRECTION PRIMARY KEY (ac_id, direction_id),

                              CONSTRAINT FK_AC_DIRECTION_AC
                                  FOREIGN KEY (ac_id) REFERENCES ac (ac_id) ON DELETE CASCADE,

                              CONSTRAINT FK_AC_DIRECTION_DIRECTION
                                  FOREIGN KEY (direction_id) REFERENCES direction (direction_id) ON DELETE CASCADE
);



