
-- liquibase formatted sql

-- changeset sigmap:1757950261-2
CREATE TABLE IF NOT EXISTS dac_prix (
    dac_id INT NOT NULL,
    dac_ver INT NOT NULL,
    monnaie_id INT NOT NULL,
    prix DECIMAL(20, 5) NOT NULL,
    CONSTRAINT PK_DAC_PRIX PRIMARY KEY (dac_id, dac_ver, monnaie_id),
    CONSTRAINT FK_DAC_PRIX_DAC FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver),
    CONSTRAINT FK_DAC_PRIX_MONNAIE FOREIGN KEY (monnaie_id) REFERENCES monnaie (monnaie_id)
    );
