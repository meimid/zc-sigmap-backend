-- liquibase formatted sql

-- changeset sigmap:1749671959050-10

DROP TABLE IF EXISTS ac_direction;

ALTER TABLE direction
    ADD COLUMN ac_id INT NOT NULL,
    ADD CONSTRAINT FK_direction_ac FOREIGN KEY (ac_id) REFERENCES ac(ac_id);