
-- liquibase formatted sql

--changeset sigmap:17496719590114-1

CREATE TABLE IF NOT EXISTS modified_dyna_docs (
    modif_dyna_docs_id INT AUTO_INCREMENT NOT NULL,
    modif_dyna_docs_ver INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    template_file_name VARCHAR(255) NOT NULL,
    status ENUM( 'ACTIF', 'NON_ACTIF') DEFAULT 'ACTIF' NOT NULL,
    is_aao TINYINT(1) NOT NULL DEFAULT 0,
    ordre INT NOT NULL,
    dac_id INT NOT NULL,
    dac_ver INT NOT NULL,
    created_at datetime DEFAULT NULL NULL,
    created_by VARCHAR(100) NULL,
    updated_at datetime DEFAULT NULL NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at datetime DEFAULT NULL NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL,

    CONSTRAINT PK_MODIF_DUNA_DOCS PRIMARY KEY (modif_dyna_docs_id),
    CONSTRAINT FK_DAC_MODIF_DUNA_DOCS FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver)
    );
