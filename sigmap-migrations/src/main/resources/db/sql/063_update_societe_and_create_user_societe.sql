-- liquibase formatted sql

-- changeset sigmap:1757950264-1

ALTER TABLE societe
    ADD COLUMN nif VARCHAR(255) NOT NULL,
    ADD COLUMN date_nif DATETIME NOT NULL,
    ADD COLUMN num_cnss INTEGER NOT NULL,
    ADD COLUMN status ENUM('VALIDEE', 'REJETEE','EN_COURS') NOT NULL DEFAULT 'EN_COURS';

-- changeset sigmap:1757950264-2
CREATE TABLE societe_activite (
    societe_id INT NOT NULL,
    activite_id INT NOT NULL,
    PRIMARY KEY (societe_id, activite_id),
    CONSTRAINT FK_SOCIETE_TO_SOCIETE_ACTIVITE
      FOREIGN KEY (societe_id) REFERENCES societe(societe_id),
    CONSTRAINT FK_ACTIVITE_TO_SOCIETE_ACTIVITE
      FOREIGN KEY (activite_id) REFERENCES activites(activite_id)
);

-- liquibase formatted sql

-- changeset sigmap:1757950264-3
CREATE TABLE IF NOT EXISTS user_societe (
    user_id VARCHAR(255) NOT NULL,
    societe_id INT NOT NULL,
    responsable TINYINT(1) NOT NULL DEFAULT 0,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    num_cin VARCHAR(255),
    date_delivrance_cin DATETIME,
    tel1 VARCHAR(255),
    tel2 VARCHAR(255),
    email VARCHAR(255),
    status ENUM('ACTIF', 'NON_ACTIF') NOT NULL DEFAULT 'NON_ACTIF',
    created_by VARCHAR(100) DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by VARCHAR(100) DEFAULT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, societe_id),
    CONSTRAINT fk_user_societe_societe
    FOREIGN KEY (societe_id) REFERENCES societe(societe_id)
    );

