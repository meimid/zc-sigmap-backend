-- liquibase formatted sql

-- changeset sigmap:1757950270-12
CREATE TABLE societe_dac (
    dac_id INT NOT NULL,
    dac_ver INT NOT NULL,
    societe_id INT NOT NULL,
    methode_paiement ENUM('EN_LIGNE', 'MANUEL') NOT NULL,
    justif_paiement_id INT NOT NULL,
    status_paiement TINYINT(1),
    created_at datetime DEFAULT NULL NULL,
    created_by VARCHAR(100) NULL,
    updated_at datetime DEFAULT NULL NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at datetime DEFAULT NULL NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL,

    PRIMARY KEY (societe_id, dac_id, dac_ver),
    CONSTRAINT FK_SOCIETE_TO_SOCIETE_DAC FOREIGN KEY (societe_id) REFERENCES societe(societe_id),
    CONSTRAINT FK_DAC_TO_SOCIETE_DAC FOREIGN KEY (dac_id, dac_ver) REFERENCES dac(dac_id, dac_ver));