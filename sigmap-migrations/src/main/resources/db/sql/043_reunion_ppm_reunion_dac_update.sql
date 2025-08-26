-- liquibase formatted sql

-- changeset sigmap:1749671959050-108

ALTER TABLE reunion_ppm ADD COLUMN observation VARCHAR(255) NULL;

-- changeset sigmap:1749671959050-109

ALTER TABLE reunion_ppm ADD COLUMN decision VARCHAR(255) NULL;

-- changeset sigmap:1749671959050-110

ALTER TABLE reunion_dac ADD COLUMN observation VARCHAR(255) NULL;

-- changeset sigmap:1749671959050-111

ALTER TABLE reunion_dac ADD COLUMN decision VARCHAR(255) NULL;

-- changeset sigmap:1749671959050-112

ALTER TABLE process_verbal DROP COLUMN decision_id;





