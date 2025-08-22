-- liquibase formatted sql

-- changeset sigmap:1749671959050-59

ALTER TABLE email_trace
    MODIFY COLUMN type_email ENUM(
    'ACTIVATION_COMPTE',
    'MODIFICATION_MOT_DE_PASSE',
    'VALIDATION_AGPM',
    'VALIDATION_PAA',
    'VALIDATION_PPM',
    'VALIDATION_DAC',
    'REUNION_INVITATION'
    ) NOT NULL;