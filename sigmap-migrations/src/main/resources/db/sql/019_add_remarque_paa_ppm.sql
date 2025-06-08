-- liquibase formatted sql

-- changeset db:1732196145835-19

-- For PAA
ALTER TABLE paa ADD COLUMN remarque VARCHAR(255) DEFAULT NULL;
-- For PPM
ALTER TABLE ppm ADD COLUMN remarque VARCHAR(255) DEFAULT NULL;
