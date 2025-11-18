-- liquibase formatted sql

-- changeset sigmap:1757950284-1

INSERT INTO ref_param (code, description, ref_type_id)
SELECT 'MR', 'Mauritanie', rt.ref_type_id
FROM ref_type rt
WHERE rt.ref_code = 'PAYS'
  AND NOT EXISTS (
    SELECT 1
    FROM ref_param rp
    WHERE rp.code = 'MR'
      AND rp.ref_type_id = rt.ref_type_id
);