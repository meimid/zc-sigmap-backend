-- liquibase formatted sql

-- changeset sigmap:1749671959093-1

ALTER TABLE ac_seuil
DROP FOREIGN KEY FK_AC_SEUIL_TO_MODE_PASSATION;


-- changeset sigmap:1749671959093-2
ALTER TABLE ac_seuil
DROP COLUMN mode_passation_id;