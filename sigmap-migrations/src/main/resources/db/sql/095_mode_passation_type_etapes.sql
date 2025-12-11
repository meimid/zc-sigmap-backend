-- liquibase formatted sql

-- changeset sigmap:1749671959096-1

ALTER TABLE mode_passation
    ADD COLUMN type ENUM('NATIONAL', 'INTERNATIONAL') NOT NULL;

-- changeset sigmap:1749671959096-2

ALTER TABLE mode_passation
    ADD COLUMN etapes ENUM('ETAPE_UNIQUE', 'DEUX_ETAPES') NOT NULL;