-- liquibase formatted sql

-- changeset sigmap:1749671959092-3

ALTER TABLE ac_seuil
DROP FOREIGN KEY FK_AC_SEUIL_TO_MODE_PASSATION;


-- changeset sigmap:1749671959092-4
ALTER TABLE ac_seuil
DROP COLUMN mode_passation_id;