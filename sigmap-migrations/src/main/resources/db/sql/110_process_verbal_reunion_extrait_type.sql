-- liquibase formatted sql

-- changeset sigmap:17496719590111-1
ALTER TABLE process_verbal_reunion_extrait
    ADD COLUMN extrait_type ENUM(
    'PAR_AC',
    'PAR_DOSSIER',
    '') NULL;

-- changeset sigmap:17496719590111-2
UPDATE process_verbal_reunion_extrait
SET extrait_type = 'PAR_AC'
WHERE extrait_type IS NULL;






