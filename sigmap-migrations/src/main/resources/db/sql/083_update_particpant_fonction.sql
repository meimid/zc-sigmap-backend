-- liquibase formatted sql

-- changeset sigmap:1757950284-1
UPDATE participant
SET fonction = 'PRESIDENT'
WHERE type = 'RESPONSABLE';

-- changeset sigmap:1757950284-2
UPDATE participant
SET fonction = 'MEMBRE'
WHERE type = 'MEMBRE';

-- changeset sigmap:1757950284-3
UPDATE participant
SET fonction = 'MEMBRE'
WHERE type = 'INVITE';

-- changeset sigmap:1757950284-4
ALTER TABLE participant
    MODIFY fonction ENUM('PRESIDENT', 'MEMBRE') NOT NULL;



