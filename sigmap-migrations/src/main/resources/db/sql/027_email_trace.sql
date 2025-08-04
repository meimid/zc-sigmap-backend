-- liquibase formatted sql

-- changeset sigmap:1749671959050-69
CREATE TABLE email_trace (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    type_email ENUM('ACTIVATION_COMPTE', 'MODIFICATION_MOT_DE_PASSE', 'VALIDATION_AGPM', 'VALIDATION_PAA', 'VALIDATION_PPM', 'VALIDATION_DAC') NOT NULL,
    objet VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    destinataire VARCHAR(255) NOT NULL,
    success_send BOOLEAN NOT NULL DEFAULT FALSE,
    date_send datetime DEFAULT NULL NULL,
    created_at datetime DEFAULT NULL NULL,
    created_by VARCHAR(100) NULL,
    updated_at datetime DEFAULT NULL NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at datetime DEFAULT NULL NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL
);