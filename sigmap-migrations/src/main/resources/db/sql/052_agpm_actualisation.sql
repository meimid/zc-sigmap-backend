-- liquibase formatted sql

-- changeset sigmap:1757950253-1
ALTER TABLE agpm
    ADD COLUMN actualise BOOLEAN,
  ADD COLUMN nbr_actualisation INT;

-- changeset sigmap:1757950253-2
UPDATE agpm
SET actualise = FALSE
WHERE actualise IS NULL;

-- changeset sigmap:1757950253-3
UPDATE agpm
SET nbr_actualisation = 0
WHERE nbr_actualisation IS NULL;

-- changeset sigmap:1757950253-4
ALTER TABLE agpm
    MODIFY actualise BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY nbr_actualisation INT NOT NULL DEFAULT 0;
