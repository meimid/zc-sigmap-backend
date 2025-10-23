
-- liquibase formatted sql

-- changeset sigmap:1757950270-3
CREATE TABLE IF NOT EXISTS dac_prix (
    dac_id INT NOT NULL,
    dac_ver INT NOT NULL,
    monnaie_id INT NOT NULL,
    prix DECIMAL(20, 5) NOT NULL,
    CONSTRAINT PK_DAC_PRIX PRIMARY KEY (dac_id, dac_ver, monnaie_id),
    CONSTRAINT FK_DAC_PRIX_DAC FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver),
    CONSTRAINT FK_DAC_PRIX_MONNAIE FOREIGN KEY (monnaie_id) REFERENCES monnaie (monnaie_id)
    );

-- liquibase formatted sql

-- changeset sigmap:1757950270-4

ALTER TABLE dac_prix
    ADD COLUMN created_at datetime DEFAULT NULL NULL,
    ADD COLUMN created_by VARCHAR(100) NULL,
    ADD COLUMN updated_at datetime DEFAULT NULL NULL,
    ADD COLUMN updated_by VARCHAR(100) NULL,
    ADD COLUMN deleted_at datetime DEFAULT NULL NULL,
    ADD COLUMN deleted_by VARCHAR(100) NULL,
    ADD COLUMN is_deleted TINYINT(1) DEFAULT 0 NOT NULL

