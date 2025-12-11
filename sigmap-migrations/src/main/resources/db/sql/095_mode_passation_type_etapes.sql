-- liquibase formatted sql

-- changeset sigmap:1749671959096-1

ALTER TABLE mode_passation
    ADD COLUMN type ENUM('National', 'International') NOT NULL;

-- changeset sigmap:1749671959096-2

ALTER TABLE mode_passation
    ADD COLUMN etapes ENUM('Etape_Unique', 'Deux_Etapes') NOT NULL;