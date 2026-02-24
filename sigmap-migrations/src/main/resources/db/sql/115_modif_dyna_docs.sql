
-- liquibase formatted sql

--changeset sigmap:17496719590116-1

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


--changeset sigmap:17496719590116-2
CREATE TABLE IF NOT EXISTS dyna_docs_copy (
    dyna_docs_copy_id INT AUTO_INCREMENT NOT NULL,
    dyna_docs_copy_ver INT NOT NULL,

    dyna_docs_copy_name VARCHAR(255) NOT NULL,
    dyna_docs_copy_template_file_name VARCHAR(255) NOT NULL,

    dyna_docs_copy_status ENUM('ACTIF', 'NON_ACTIF') DEFAULT 'ACTIF' NOT NULL,
    dyna_docs_copy_is_aao TINYINT(1) NOT NULL DEFAULT 0,
    dyna_docs_copy_ordre INT NOT NULL,

    dac_id INT NOT NULL,
    dac_ver INT NOT NULL,

    dyna_docs_id INT NOT NULL,
    dyna_docs_ver INT NOT NULL,

    created_at DATETIME DEFAULT NULL,
    created_by VARCHAR(100) NULL,
    updated_at DATETIME DEFAULT NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL,

    CONSTRAINT PK_DYNA_DOCS_COPY PRIMARY KEY (dyna_docs_copy_id),

    CONSTRAINT FK_DAC_DYNA_DOCS_COPY FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver),

    CONSTRAINT FK_DYNA_DOCS_COPY_DYNA_DOCS FOREIGN KEY (dyna_docs_id, dyna_docs_ver) REFERENCES dyna_docs (dyna_docs_id, dyna_docs_ver)

    );


--changeset sigmap:17496719590116-3
ALTER TABLE dyna_docs_copy
ADD COLUMN is_static_dyna_docs_copy TINYINT(1) NOT NULL DEFAULT 0;

