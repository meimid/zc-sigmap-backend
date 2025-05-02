-- liquibase formatted sql

-- changeset db:1732196145835-6

INSERT INTO ref_param (ref_param_id,code, description, ref_type_id, created_at, created_by, is_deleted)
VALUES
    (4,'A_PUBLIEE', 'À publiée', 1, NOW(), 'admin', 0),
    (5,'A_RECTIFIEE', 'À rectifiée', 1, NOW(), 'admin', 0);