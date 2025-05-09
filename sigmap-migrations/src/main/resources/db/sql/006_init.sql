-- liquibase formatted sql

-- changeset db:1732196145835-4

-- Insert ref_type for status
INSERT INTO ref_type (ref_type_id, description, created_at,  is_deleted)
VALUES (1, 'Status', NOW(),  0);

INSERT INTO ref_param (ref_param_id,code, description, ref_type_id, created_at, created_by, is_deleted)
VALUES
    (1,'BROUILLON', 'Brouillon', 1, NOW(), 'admin', 0),
    (2,'EN_TRAITEMENT', 'En cours de traitemen', 1, NOW(), 'admin', 0),
    (3,'VALIDE', 'Validé', 1, NOW(), 'admin', 0),
    (4,'PUBLIEE', 'Publiée', 1, NOW(), 'admin', 0);

ALTER TABLE agpm ADD COLUMN process_instance_id VARCHAR(255) DEFAULT NULL;


