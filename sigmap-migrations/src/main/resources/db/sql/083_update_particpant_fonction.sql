-- liquibase formatted sql

-- changeset sigmap:1757950284-1
ALTER TABLE participant
    ADD COLUMN fonction_reunion ENUM('PRESIDENT', 'MEMBRE') NOT NULL DEFAULT 'MEMBRE';




