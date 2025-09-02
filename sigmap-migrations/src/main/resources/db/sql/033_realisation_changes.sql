-- liquibase formatted sql

-- changeset sigmap:1749671959050-94
ALTER TABLE realisation
DROP COLUMN service_id,
DROP COLUMN direction_id,
DROP FOREIGN KEY fk_realisation_service_id,
DROP FOREIGN KEY fk_realisation_direction_id,
ADD COLUMN beneficiaire VARCHAR(255);