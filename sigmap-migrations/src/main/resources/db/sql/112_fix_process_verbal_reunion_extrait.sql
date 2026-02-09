-- liquibase formatted sql

-- changeset sigmap:17496719590113-1
CREATE TABLE process_verbal_reunion_extrait_ppm (
    extrait_id INT NOT NULL,
    pv_id INT NOT NULL,
    ac_id INT NOT NULL,
    ppm_id INT NOT NULL,
    ppm_ver INT NOT NULL
);

-- changeset sigmap:17496719590113-2
ALTER TABLE process_verbal_reunion_extrait_ppm
    ADD CONSTRAINT PK_PV_REUNION_EXTRAIT_PPM
        PRIMARY KEY (extrait_id, pv_id, ac_id, ppm_id, ppm_ver);

-- changeset sigmap:17496719590113-3
ALTER TABLE process_verbal_reunion_extrait_ppm
    ADD CONSTRAINT FK_PVRE_EXTRAIT_PPM_EXTRAIT
        FOREIGN KEY (extrait_id, pv_id, ac_id)
            REFERENCES process_verbal_reunion_extrait (extrait_id, pv_id, ac_id);

-- changeset sigmap:17496719590113-4
ALTER TABLE process_verbal_reunion_extrait_ppm
    ADD CONSTRAINT FK_PVRE_EXTRAIT_PPM_PPM
        FOREIGN KEY (ppm_id, ppm_ver)
            REFERENCES ppm (ppm_id, ppm_ver);

-- changeset sigmap:17496719590113-5
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

-- changeset sigmap:17496719590113-6
DELETE e
FROM process_verbal_reunion_extrait e
JOIN (
    SELECT pv_id, ac_id, MIN(extrait_id) keep_id
    FROM process_verbal_reunion_extrait
    WHERE extrait_type = 'PAR_AC'
    GROUP BY pv_id, ac_id
) k
  ON e.pv_id = k.pv_id
 AND e.ac_id = k.ac_id
WHERE e.extrait_type = 'PAR_AC'
  AND e.extrait_id <> k.keep_id;


-- changeset sigmap:17496719590113-7
ALTER TABLE process_verbal_reunion_extrait
DROP FOREIGN KEY FK_EXTRAIT_PPM;

-- changeset sigmap:17496719590113-8
DROP INDEX FK_EXTRAIT_PPM
    ON process_verbal_reunion_extrait;




-- changeset sigmap:17496719590113-9
ALTER TABLE process_verbal_reunion_extrait
DROP COLUMN ppm_id,
DROP COLUMN ppm_ver;


-- changeset sigmap:17496719590113-10
CREATE TABLE process_verbal_reunion_extrait_dac (
    extrait_id INT NOT NULL,
    pv_id INT NOT NULL,
    ac_id INT NOT NULL,
    dac_id INT NOT NULL,
    dac_ver INT NOT NULL
);

-- changeset sigmap:17496719590113-11
ALTER TABLE process_verbal_reunion_extrait_dac
    ADD CONSTRAINT PK_PV_REUNION_EXTRAIT_DAC
        PRIMARY KEY (extrait_id, pv_id, ac_id, dac_id, dac_ver);

-- changeset sigmap:17496719590113-12
ALTER TABLE process_verbal_reunion_extrait_dac
    ADD CONSTRAINT FK_PVRE_EXTRAIT_DAC_EXTRAIT
        FOREIGN KEY (extrait_id, pv_id, ac_id)
            REFERENCES process_verbal_reunion_extrait (extrait_id, pv_id, ac_id);

-- changeset sigmap:17496719590113-13
ALTER TABLE process_verbal_reunion_extrait_dac
    ADD CONSTRAINT FK_PVRE_EXTRAIT_DAC_DAC
        FOREIGN KEY (dac_id, dac_ver)
            REFERENCES dac (dac_id, dac_ver);


-- changeset sigmap:17496719590113-14
ALTER TABLE reunion
    ADD COLUMN validated_by_user_id VARCHAR(255) NULL;

-- changeset sigmap:17496719590113-15
ALTER TABLE reunion
    ADD COLUMN validated_by_user_full_name VARCHAR(255) NULL;

-- changeset sigmap:17496719590113-16
ALTER TABLE reunion
    ADD COLUMN validated_at DATETIME NULL;

-- changeset sigmap:17496719590113-17
ALTER TABLE reunion
    ADD COLUMN closed_by_user_id VARCHAR(255) NULL;

-- changeset sigmap:17496719590113-18
ALTER TABLE reunion
    ADD COLUMN closed_by_user_full_name VARCHAR(255) NULL;

-- changeset sigmap:17496719590113-19
ALTER TABLE reunion
    ADD COLUMN closed_at DATETIME NULL;









