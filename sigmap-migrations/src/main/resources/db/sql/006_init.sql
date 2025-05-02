-- liquibase formatted sql

-- changeset db:1732196145835-4

-- Insert ref_type for status
INSERT INTO ref_type (ref_type_id, description, created_at,  is_deleted)
VALUES (1, 'Status', NOW(),  0);

-- Insert ref_param entries for status (publiee, brouillon, a_valide)
INSERT INTO ref_param (code, description, ref_type_id, created_at, created_by, is_deleted)
VALUES
    ('PUBLIEE', 'Publiée', 1, NOW(), 'admin', 0),
    ('BROUILLON', 'Brouillon', 1, NOW(), 'admin', 0),
    ('A_VALIDE', 'À valider', 1, NOW(), 'admin', 0);