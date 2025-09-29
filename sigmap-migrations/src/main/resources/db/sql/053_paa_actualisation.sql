-- liquibase formatted sql

-- changeset sigmap:1757950254-1
ALTER TABLE paa
    ADD COLUMN actualise BOOLEAN,
  ADD COLUMN nbr_actualisation INT;

-- changeset sigmap:1757950254-2
UPDATE paa
SET actualise = FALSE
WHERE actualise IS NULL;

-- changeset sigmap:1757950254-3
UPDATE paa
SET nbr_actualisation = 0
WHERE nbr_actualisation IS NULL;

-- changeset sigmap:1757950254-4
ALTER TABLE paa
    MODIFY actualise BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY nbr_actualisation INT NOT NULL DEFAULT 0;
