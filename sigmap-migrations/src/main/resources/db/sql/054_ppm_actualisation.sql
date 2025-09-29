-- liquibase formatted sql

-- changeset sigmap:1757950255-1
ALTER TABLE ppm
    ADD COLUMN actualise BOOLEAN,
  ADD COLUMN nbr_actualisation INT;

-- changeset sigmap:1757950255-2
UPDATE ppm
SET actualise = FALSE
WHERE actualise IS NULL;

-- changeset sigmap:1757950255-3
UPDATE ppm
SET nbr_actualisation = 0
WHERE nbr_actualisation IS NULL;

-- changeset sigmap:1757950255-4
ALTER TABLE ppm
    MODIFY actualise BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY nbr_actualisation INT NOT NULL DEFAULT 0;
