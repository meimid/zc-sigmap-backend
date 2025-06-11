-- liquibase formatted sql

-- changeset sigmap:1749671959050-125
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'TYPE_SOCIETE', 'Type de société', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'TYPE_SOCIETE'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'PAYS', 'Pays', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'PAYS'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'CATEGORIE_SOMISSIONAIRE', 'Catégorie de soumissionnaire', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'CATEGORIE_SOMISSIONAIRE'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'TYPE_COMITE', 'Type de commission', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'TYPE_COMITE'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'TYPE_ADDRESS', 'Type d’adresse', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'TYPE_ADDRESS'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'SERVICE', 'Bénéficiaire', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'SERVICE'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'TYPE_MARCHER', 'Type de marché', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'TYPE_MARCHER'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'SOURCE_FINANCEMENT', 'Source de financement', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'SOURCE_FINANCEMENT'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'MONAIS', 'Monnaie', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'MONAIS'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'MODE_SELECTION', 'Mode de sélection', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'MODE_SELECTION'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'DIRECTION', 'Direction', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'DIRECTION'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'MODE_PASSATION', 'Mode de passation', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'MODE_PASSATION'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'TUTEL', 'Tutelle', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'TUTEL'
);
 
INSERT INTO ref_type (ref_code, description, system_type)
SELECT 'CATEGORIE_DAC', 'Catégorie - DAC', false
WHERE NOT EXISTS (
    SELECT 1 FROM ref_type WHERE ref_code = 'CATEGORIE_DAC'
);
