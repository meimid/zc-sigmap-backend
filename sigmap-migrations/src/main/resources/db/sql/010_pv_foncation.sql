
-- liquibase formatted sql

-- changeset sigmap:1749671959050-14
ALTER Table pv_membre_present ADD COLUMN fonction ENUM('RESPONSABLE', 'MEMEBRE', 'INVITE') NOT NULL DEFAULT 'INVITE';
ALTER TABLE pv_membre_present MODIFY COLUMN fonction_id INT NULL;
