-- liquibase formatted sql

-- changeset sigmap:1757950259-1
UPDATE ref_type
SET ref_code   = 'CATEGORIE_ACTIVITE',
    description = 'Catégories Activités'
WHERE ref_code = 'CATEGORIE_SOMISSIONAIRE';

-- changeset sigmap:1757950259-2
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'TYPE_AGREMENT', 'Type Agrément', false
    WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'TYPE_AGREMENT'
);

-- changeset sigmap:1757950259-3
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'ORGANISME_DE_DELIVRANCE_AGREMENTS', 'Organisme de Délivrance Agréments', false
    WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'ORGANISME_DE_DELIVRANCE_AGREMENTS'
);


-- changeset sigmap:1757950259-4

CREATE TABLE IF NOT EXISTS activites (
    activite_id INT AUTO_INCREMENT NOT NULL,
    libelle VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT NULL,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by VARCHAR(100) DEFAULT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT PK_ACTIVITE PRIMARY KEY (activite_id)
    );

-- changeset sigmap:1757950259-5

CREATE TABLE IF NOT EXISTS activites_categories_activites (
    activite_id INT NOT NULL,
    categories_activite_id INT NOT NULL,
    CONSTRAINT PK_ACA PRIMARY KEY (activite_id, categories_activite_id)
    );

-- changeset sigmap:1757950259-6

ALTER TABLE activites_categories_activites ADD CONSTRAINT FK_ACA_ACTIVITE FOREIGN KEY (activite_id) REFERENCES activites(activite_id);

-- changeset sigmap:1757950259-7

ALTER TABLE activites_categories_activites ADD CONSTRAINT FK_ACA_CATEGORIE FOREIGN KEY (categories_activite_id) REFERENCES ref_param(ref_param_id);

