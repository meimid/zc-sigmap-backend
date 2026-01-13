-- liquibase formatted sql

-- changeset sigmap:17496719590109-1
CREATE TABLE IF NOT EXISTS source_financement (
    source_financement_id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    bailleur_de_fonds TINYINT(1) NOT NULL DEFAULT FALSE,
    created_at DATETIME(0) NULL,
    created_by VARCHAR(100) NULL,
    updated_at DATETIME(0) NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at DATETIME(0) NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) NULL DEFAULT FALSE,
    PRIMARY KEY (source_financement_id),
    UNIQUE INDEX source_financement_code_idx (code)
);

-- changeset sigmap:17496719590109-2
INSERT INTO source_financement (code, description, bailleur_de_fonds, created_at, created_by)
SELECT rp.code, rp.description, FALSE, rp.created_at, rp.created_by
FROM ref_param rp
INNER JOIN ref_type rt ON rp.ref_type_id = rt.ref_type_id
WHERE rt.ref_code = 'SOURCE_FINANCEMENT';

-- changeset sigmap:17496719590109-3
CREATE TEMPORARY TABLE sf_mapping AS
SELECT rp.ref_param_id as old_id, sf.source_financement_id as new_id
FROM ref_param rp
INNER JOIN ref_type rt ON rp.ref_type_id = rt.ref_type_id
INNER JOIN source_financement sf ON rp.code = sf.code
WHERE rt.ref_code = 'SOURCE_FINANCEMENT';

-- changeset sigmap:17496719590109-4
ALTER TABLE finacement DROP FOREIGN KEY FK_FINANCEMENT_TO_SOURCE_FINANCEMENT;

-- changeset sigmap:17496719590109-5
UPDATE finacement f
INNER JOIN sf_mapping m ON f.source_de_financement_id = m.old_id
SET f.source_de_financement_id = m.new_id;

-- changeset sigmap:17496719590109-6
ALTER TABLE finacement ADD CONSTRAINT FK_FINANCEMENT_TO_SOURCE_FINANCEMENT
    FOREIGN KEY (source_de_financement_id) REFERENCES source_financement (source_financement_id);
