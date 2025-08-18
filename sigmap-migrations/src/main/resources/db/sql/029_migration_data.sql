-- liquibase formatted sql

-- changeset sigmap:1749671959050-71
-- comment: migration data to table monnaie from refParam

INSERT INTO monnaie (
    code,
    description,
    taux,
    created_at,
    created_by,
    updated_at,
    updated_by,
    deleted_at,
    deleted_by,
    is_deleted
)
SELECT
    rp.code,
    rp.description,
    1.000 AS taux,
    rp.created_at,
    rp.created_by,
    rp.updated_at,
    rp.updated_by,
    rp.deleted_at,
    rp.deleted_by,
    rp.is_deleted
FROM ref_param rp
         JOIN ref_type rt ON rp.ref_type_id = rt.ref_type_id
WHERE rt.ref_code = 'MONAIS' AND rt.is_deleted = 0 AND rp.is_deleted = 0;

-- changeset sigmap:1749671959050-72
-- comment: migration old colomn monnais_id (ref_param)  vers nouvelle colomn monnaie_id (monnaie)

UPDATE realisation r
    JOIN ref_param rp ON r.monais_id = rp.ref_param_id
    JOIN ref_type rt ON rp.ref_type_id = rt.ref_type_id AND rt.ref_code = 'MONAIS' AND rt.is_deleted = 0
    JOIN monnaie m ON m.code = rp.code AND m.is_deleted = 0
    SET r.monnaie_id = m.monnaie_id
WHERE r.monnaie_id IS NULL;


-- changeset sigmap:1749671959050-73
-- CPMP

INSERT INTO ref_param (code, description, ref_type_id, is_deleted)
SELECT 'CPMP', 'CPMP', rt.ref_type_id, 0
FROM ref_type rt
WHERE rt.ref_code = 'TYPE_COMITE'
  AND NOT EXISTS (
        SELECT 1 FROM ref_param rp
        WHERE rp.code = 'CPMP' AND rp.ref_type_id = rt.ref_type_id
    );

-- changeset sigmap:1749671959050-74
-- CNCMP-P

INSERT INTO ref_param (code, description, ref_type_id, is_deleted)
SELECT 'CNCMP-P', 'CNCMP Permanente', rt.ref_type_id, 0
FROM ref_type rt
WHERE rt.ref_code = 'TYPE_COMITE'
  AND NOT EXISTS (
        SELECT 1 FROM ref_param rp
        WHERE rp.code = 'CNCMP-P' AND rp.ref_type_id = rt.ref_type_id
    );

-- changeset sigmap:1749671959050-75
-- CNCMP-S

INSERT INTO ref_param (code, description, ref_type_id, is_deleted)
SELECT 'CNCMP-S', 'CNCMP Spécialisé', rt.ref_type_id, 0
FROM ref_type rt
WHERE rt.ref_code = 'TYPE_COMITE'
  AND NOT EXISTS (
        SELECT 1 FROM ref_param rp
        WHERE rp.code = 'CNCMP-S' AND rp.ref_type_id = rt.ref_type_id
    );


-- changeset sigmap:1749671959050-76
-- comment: Disable REF_TYPE & REF_PARAM CHANGED

UPDATE ref_type
SET system_type = true
WHERE ref_code IN (
                   'SERVICE',
                   'MONAIS',
                   'TYPE_COMITE',
                   'MODE_SELECTION',
                   'DIRECTION',
                   'MODE_PASSATION',
                   'TUTEL'
);