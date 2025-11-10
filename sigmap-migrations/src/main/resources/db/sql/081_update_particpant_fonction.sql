-- liquibase formatted sql

-- changeset sigmap:1757950282-1
UPDATE participant
SET fonction = 'PRESIDENT'
WHERE type = 'RESPONSABLE';

-- changeset sigmap:1757950282-2
UPDATE participant
SET fonction = 'MEMBRE'
WHERE type = 'MEMBRE';

-- changeset sigmap:1757950282-3
UPDATE participant
SET fonction = 'MEMBRE'
WHERE type = 'INVITE';

-- changeset sigmap:1757950282-4
ALTER TABLE participant
    MODIFY fonction ENUM('PRESIDENT', 'MEMBRE') NOT NULL;



