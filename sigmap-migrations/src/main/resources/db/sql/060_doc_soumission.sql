-- liquibase formatted sql

-- changeset sigmap:1757950261-1

CREATE TABLE IF NOT EXISTS doc_soumission (
    doc_soumission_id INT AUTO_INCREMENT NOT NULL,
    nom VARCHAR(255) NOT NULL,
    type_dossier ENUM('ADMINISTRATIVE', 'FINANCIERE', 'TECHNIQUE') NOT NULL,
    CONSTRAINT PK_DOC_SOUMISSION PRIMARY KEY (doc_soumission_id)
    );
