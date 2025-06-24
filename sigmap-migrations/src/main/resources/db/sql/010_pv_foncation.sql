
-- liquibase formatted sql

-- changeset sigmap:1749671959050-14
ALTER Table pv_membre_present ADD COLUMN fonction ENUM('RESPONSABLE', 'MEMEBRE', 'INVITE') NOT NULL DEFAULT 'INVITE';
ALTER Table pv_membre_present MODIFY COLMUN fonction_id INT NULL;

