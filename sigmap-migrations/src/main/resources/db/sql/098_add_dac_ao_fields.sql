-- liquibase formatted sql

-- changeset sigmap:1749671959098-1
ALTER TABLE dac ADD COLUMN IF NOT EXISTS responsable_dac_id VARCHAR(255) NULL;

ALTER TABLE dac ADD COLUMN IF NOT EXISTS conditions_participation LONGTEXT NULL;

ALTER TABLE dac ADD COLUMN IF NOT EXISTS etape_dac VARCHAR(50) NULL;

ALTER TABLE dac ADD COLUMN IF NOT EXISTS appel_offre_id INT NULL;

-- changeset sigmap:1749671959098-2
ALTER TABLE dac ADD CONSTRAINT fk_dac_appel_offre 
    FOREIGN KEY (appel_offre_id) 
    REFERENCES appel_offre(appel_offre_id)
    ON DELETE SET NULL
    ON UPDATE NO ACTION;

-- changeset sigmap:1749671959098-3
CREATE INDEX IF NOT EXISTS idx_dac_appel_offre_id ON dac(appel_offre_id);
CREATE INDEX IF NOT EXISTS idx_dac_responsable_id ON dac(responsable_dac_id);

-- changeset sigmap:1749671959098-4
ALTER TABLE dac DROP FOREIGN KEY IF EXISTS FK_DAC_PPM;
ALTER TABLE dac DROP FOREIGN KEY IF EXISTS FK_DAC_REF_PARAM;
ALTER TABLE dac DROP FOREIGN KEY IF EXISTS FK_DAC_REALISATION;
ALTER TABLE dac DROP FOREIGN KEY IF EXISTS FK_DAC_MODE_PASSATION
-- changeset sigmap:1749671959098-5
DROP INDEX IF EXISTS FK_DAC_PPM ON dac;
DROP INDEX IF EXISTS FK_DAC_REF_PARAM ON dac;

-- changeset sigmap:1749671959098-6
ALTER TABLE dac DROP COLUMN IF EXISTS ppm_id;
ALTER TABLE dac DROP COLUMN IF EXISTS ppm_ver;
ALTER TABLE dac DROP COLUMN IF EXISTS category_id;
ALTER TABLE dac DROP COLUMN IF EXISTS mode_passation_id;
ALTER TABLE dac DROP COLUMN IF EXISTS realisation_id;
ALTER TABLE dac DROP COLUMN IF EXISTS realisation_ver;
ALTER TABLE dac DROP COLUMN IF EXISTS parent_dac_id;

-- changeset sigmap:1749671959098-7
CREATE TABLE dac_shortlist (
                               id INT AUTO_INCREMENT NOT NULL,
                               dac_id INT NOT NULL,
                               dac_ver INT NOT NULL,
                               societe_id INT NOT NULL,
                               created_at datetime DEFAULT NULL,
                               created_by VARCHAR(100) NULL,
                               updated_at datetime DEFAULT NULL,
                               updated_by VARCHAR(100) NULL,
                               deleted_at datetime DEFAULT NULL,
                               deleted_by VARCHAR(100) NULL,
                               is_deleted TINYINT(1) DEFAULT 0 NOT NULL,
                               CONSTRAINT PK_DAC_SHORTLIST PRIMARY KEY (id)
);

-- changeset sigmap:1749671959098-8
ALTER TABLE dac_shortlist ADD CONSTRAINT FK_DAC_SHORTLIST_DAC FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE dac_shortlist ADD CONSTRAINT FK_DAC_SHORTLIST_SOCIETE FOREIGN KEY (societe_id) REFERENCES societe (societe_id) ON DELETE NO ACTION ON UPDATE NO ACTION;
