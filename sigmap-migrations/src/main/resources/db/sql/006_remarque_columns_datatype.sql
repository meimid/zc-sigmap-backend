-- liquibase formatted sql

-- changeset sigmap:1749671959050-6
ALTER TABLE agpm MODIFY COLUMN remarque TEXT NULL;

-- changeset sigmap:1749671959050-7
ALTER TABLE paa MODIFY COLUMN remarque TEXT NULL;

-- changeset sigmap:1749671959050-8
ALTER TABLE ppm MODIFY COLUMN remarque TEXT NULL;
