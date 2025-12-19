-- liquibase formatted sql

-- =========================================
-- TYPE_DOSSIER REFERENCE TABLE
-- =========================================

-- changeset sigmap:1749671959099-1
CREATE TABLE IF NOT EXISTS type_dossier (
                                            type_dossier_id INT AUTO_INCREMENT PRIMARY KEY,
                                            code VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL
    );

-- changeset sigmap:1749671959099-2
INSERT INTO type_dossier (code, libelle, description)
SELECT 'ADM', 'Administratif', 'Évaluation des aspects administratifs du dossier'
    WHERE NOT EXISTS (SELECT 1 FROM type_dossier WHERE code = 'ADM');

-- changeset sigmap:1749671959099-3
INSERT INTO type_dossier (code, libelle, description)
SELECT 'TECH', 'Technique', 'Évaluation des aspects techniques de l''offre'
    WHERE NOT EXISTS (SELECT 1 FROM type_dossier WHERE code = 'TECH');

-- changeset sigmap:1749671959099-4
INSERT INTO type_dossier (code, libelle, description)
SELECT 'FIN', 'Financier', 'Évaluation des aspects financiers et budgétaires'
    WHERE NOT EXISTS (SELECT 1 FROM type_dossier WHERE code = 'FIN');

-- =========================================
-- DOC_SOUMISSION MIGRATION (ENUM → FK)
-- =========================================

-- changeset sigmap:1749671959099-5
ALTER TABLE doc_soumission
    ADD COLUMN type_dossier_id INT;

-- changeset sigmap:1749671959099-6
UPDATE doc_soumission
SET type_dossier_id = CASE
                          WHEN UPPER(type_dossier) = 'ADMINISTRATIVE'
                              THEN (SELECT type_dossier_id FROM type_dossier WHERE code = 'ADM')
                          WHEN UPPER(type_dossier) = 'FINANCIERE'
                              THEN (SELECT type_dossier_id FROM type_dossier WHERE code = 'FIN')
                          WHEN UPPER(type_dossier) = 'TECHNIQUE'
                              THEN (SELECT type_dossier_id FROM type_dossier WHERE code = 'TECH')
                          ELSE NULL
    END
WHERE type_dossier IS NOT NULL;

-- changeset sigmap:1749671959099-7
DELETE FROM doc_soumission
WHERE type_dossier IS NULL
  AND type_dossier_id IS NULL;

-- changeset sigmap:1749671959099-8
ALTER TABLE doc_soumission
    MODIFY type_dossier_id INT NOT NULL;

-- changeset sigmap:1749671959099-9
ALTER TABLE doc_soumission
    ADD CONSTRAINT fk_doc_soumission_type_dossier
        FOREIGN KEY (type_dossier_id)
            REFERENCES type_dossier(type_dossier_id);

-- changeset sigmap:1749671959099-10
ALTER TABLE doc_soumission
DROP COLUMN type_dossier;

-- =========================================
-- DYNA_FORM MIGRATION (ENUM → FK)
-- =========================================

-- changeset sigmap:1749671959099-11
ALTER TABLE dyna_form
    ADD COLUMN type_dossier_id INT;

-- changeset sigmap:1749671959099-12
UPDATE dyna_form
SET type_dossier_id = CASE
                          WHEN UPPER(type_dossier) = 'ADMINISTRATIVE'
                              THEN (SELECT type_dossier_id FROM type_dossier WHERE code = 'ADM')
                          WHEN UPPER(type_dossier) = 'FINANCIERE'
                              THEN (SELECT type_dossier_id FROM type_dossier WHERE code = 'FIN')
                          WHEN UPPER(type_dossier) = 'TECHNIQUE'
                              THEN (SELECT type_dossier_id FROM type_dossier WHERE code = 'TECH')
                          ELSE NULL
    END
WHERE type_dossier IS NOT NULL;

-- changeset sigmap:1749671959099-13
DELETE FROM dyna_form
WHERE type_dossier IS NULL
  AND type_dossier_id IS NULL;

-- changeset sigmap:1749671959099-14
ALTER TABLE dyna_form
    MODIFY type_dossier_id INT NOT NULL;

-- changeset sigmap:1749671959099-15
ALTER TABLE dyna_form
    ADD CONSTRAINT fk_dyna_form_type_dossier
        FOREIGN KEY (type_dossier_id)
            REFERENCES type_dossier(type_dossier_id);

-- changeset sigmap:1749671959099-16
ALTER TABLE dyna_form
DROP COLUMN type_dossier;
