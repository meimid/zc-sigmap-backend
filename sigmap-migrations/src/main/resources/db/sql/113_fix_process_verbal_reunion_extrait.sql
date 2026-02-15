-- liquibase formatted sql

-- changeset sigmap:17496719590114-1
CREATE TABLE IF NOT EXISTS process_verbal_reunion_extrait_ppm (
    extrait_id INT NOT NULL,
    pv_id INT NOT NULL,
    ac_id INT NOT NULL,
    ppm_id INT NOT NULL,
    ppm_ver INT NOT NULL
);

-- changeset sigmap:17496719590114-2
ALTER TABLE process_verbal_reunion_extrait_ppm
    ADD CONSTRAINT PK_PV_REUNION_EXTRAIT_PPM
        PRIMARY KEY (extrait_id, pv_id, ac_id, ppm_id, ppm_ver);

-- changeset sigmap:17496719590114-3
ALTER TABLE process_verbal_reunion_extrait_ppm
    ADD CONSTRAINT FK_PVRE_EXTRAIT_PPM_EXTRAIT
        FOREIGN KEY (extrait_id, pv_id, ac_id)
            REFERENCES process_verbal_reunion_extrait (extrait_id, pv_id, ac_id);

-- changeset sigmap:17496719590114-4
ALTER TABLE process_verbal_reunion_extrait_ppm
    ADD CONSTRAINT FK_PVRE_EXTRAIT_PPM_PPM
        FOREIGN KEY (ppm_id, ppm_ver)
            REFERENCES ppm (ppm_id, ppm_ver);

-- changeset sigmap:17496719590114-5
INSERT INTO process_verbal_reunion_extrait_ppm
(extrait_id, pv_id, ac_id, ppm_id, ppm_ver)
SELECT
    e.extrait_id,
    e.pv_id,
    e.ac_id,
    e.ppm_id,
    e.ppm_ver
FROM process_verbal_reunion_extrait e
WHERE e.extrait_type = 'PAR_DOSSIER'
  AND e.ppm_id IS NOT NULL
  AND e.ppm_ver IS NOT NULL;

-- changeset sigmap:17496719590114-6
ALTER TABLE process_verbal_reunion_extrait
    ADD COLUMN IF NOT EXISTS created_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS deleted_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS is_deleted TINYINT(1) NOT NULL DEFAULT 0;

-- changeset sigmap:17496719590114-7
UPDATE process_verbal_reunion_extrait e
    JOIN process_verbal_reunion pv ON pv.pv_id = e.pv_id
    SET
        e.created_at = COALESCE(e.created_at, pv.created_at),
        e.created_by = COALESCE(e.created_by, pv.created_by),
        e.updated_at = COALESCE(e.updated_at, pv.updated_at),
        e.updated_by = COALESCE(e.updated_by, pv.updated_by),
        e.deleted_at = COALESCE(e.deleted_at, pv.deleted_at),
        e.deleted_by = COALESCE(e.deleted_by, pv.deleted_by),
        e.is_deleted = COALESCE(e.is_deleted, pv.is_deleted, 0);

-- changeset sigmap:17496719590114-8
UPDATE process_verbal_reunion_extrait e
    JOIN (
    SELECT pv_id, ac_id, MIN(extrait_id) AS keep_id
    FROM process_verbal_reunion_extrait
    WHERE extrait_type = 'PAR_AC'
    AND is_deleted = 0
    GROUP BY pv_id, ac_id
    ) k
ON e.pv_id = k.pv_id
    AND e.ac_id = k.ac_id
    SET
        e.is_deleted = 1,
        e.deleted_at = COALESCE(e.deleted_at, NOW()),
        e.deleted_by = COALESCE(e.deleted_by, 'system')
WHERE e.extrait_type = 'PAR_AC'
  AND e.extrait_id <> k.keep_id
  AND e.is_deleted = 0;

-- changeset sigmap:17496719590114-9
ALTER TABLE process_verbal_reunion_extrait
DROP FOREIGN KEY FK_EXTRAIT_PPM;

-- changeset sigmap:17496719590114-10
DROP INDEX FK_EXTRAIT_PPM
    ON process_verbal_reunion_extrait;

-- changeset sigmap:17496719590114-11
ALTER TABLE process_verbal_reunion_extrait
DROP COLUMN ppm_id,
DROP COLUMN ppm_ver;


-- changeset sigmap:17496719590114-12
CREATE TABLE IF NOT EXISTS process_verbal_reunion_extrait_dac (
    extrait_id INT NOT NULL,
    pv_id INT NOT NULL,
    ac_id INT NOT NULL,
    dac_id INT NOT NULL,
    dac_ver INT NOT NULL
);

-- changeset sigmap:17496719590114-13
ALTER TABLE process_verbal_reunion_extrait_dac
    ADD CONSTRAINT PK_PV_REUNION_EXTRAIT_DAC
        PRIMARY KEY (extrait_id, pv_id, ac_id, dac_id, dac_ver);

-- changeset sigmap:17496719590114-14
ALTER TABLE process_verbal_reunion_extrait_dac
    ADD CONSTRAINT FK_PVRE_EXTRAIT_DAC_EXTRAIT
        FOREIGN KEY (extrait_id, pv_id, ac_id)
            REFERENCES process_verbal_reunion_extrait (extrait_id, pv_id, ac_id);

-- changeset sigmap:17496719590114-15
ALTER TABLE process_verbal_reunion_extrait_dac
    ADD CONSTRAINT FK_PVRE_EXTRAIT_DAC_DAC
        FOREIGN KEY (dac_id, dac_ver)
            REFERENCES dac (dac_id, dac_ver);


-- changeset sigmap:17496719590114-16
ALTER TABLE reunion
    ADD COLUMN IF NOT EXISTS validated_by_user_id VARCHAR(255) NULL;

-- changeset sigmap:17496719590114-17
ALTER TABLE reunion
    ADD COLUMN IF NOT EXISTS validated_by_user_full_name VARCHAR(255) NULL;

-- changeset sigmap:17496719590114-18
ALTER TABLE reunion
    ADD COLUMN IF NOT EXISTS validated_at DATETIME NULL;

-- changeset sigmap:17496719590114-19
ALTER TABLE reunion
    ADD COLUMN  IF NOT EXISTS closed_by_user_id VARCHAR(255) NULL;

-- changeset sigmap:17496719590114-20
ALTER TABLE reunion
    ADD COLUMN  IF NOT EXISTS closed_by_user_full_name VARCHAR(255) NULL;

-- changeset sigmap:17496719590114-21
ALTER TABLE reunion
    ADD COLUMN IF NOT EXISTS closed_at DATETIME NULL;









