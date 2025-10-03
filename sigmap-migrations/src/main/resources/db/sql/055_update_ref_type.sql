-- liquibase formatted sql

-- changeset sigmap:1757950255-5
UPDATE ref_type
SET ref_code   = 'CATEGORIE_ACTIVITE',
    description = 'Catégories Activités'
WHERE ref_code = 'CATEGORIE_SOMISSIONAIRE';

-- changeset sigmap:1757950255-6
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'TYPE_AGREMENT', 'Type Agrément', false
    WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'TYPE_AGREMENT'
);

-- changeset sigmap:1757950255-7
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'ORGANISME_DE_DELIVRANCE_AGREMENTS', 'Organisme de Délivrance Agréments', false
    WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'ORGANISME_DE_DELIVRANCE_AGREMENTS'
);


