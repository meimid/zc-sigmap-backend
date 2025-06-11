-- liquibase formatted sql

-- changeset db:1732196145835-18

-- For PAA
ALTER TABLE paa ADD COLUMN mode_passation_id INTEGER;

-- For PPM
ALTER TABLE ppm ADD COLUMN mode_passation_id INTEGER;
