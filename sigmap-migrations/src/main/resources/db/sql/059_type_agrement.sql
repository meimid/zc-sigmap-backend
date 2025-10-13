-- liquibase formatted sql

-- changeset sigmap:1757950260-1

DELETE FROM ref_type
WHERE ref_code = 'TYPE_AGREMENT';

-- changeset sigmap:1757950260-2

CREATE TABLE IF NOT EXISTS type_agrement (
    type_agrement_id INT AUTO_INCREMENT NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    organisme_de_delivrance_agrements_id INT NOT NULL,
    created_at DATETIME DEFAULT NULL,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by VARCHAR(100) DEFAULT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT PK_TYPE_AGREMENT PRIMARY KEY (type_agrement_id),
    CONSTRAINT FK_ORGANISME_DE_DELIVRANCE_AGREMENTS FOREIGN KEY (organisme_de_delivrance_agrements_id)
    REFERENCES ref_type(ref_type_id)
    );