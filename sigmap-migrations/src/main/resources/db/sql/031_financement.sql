-- liquibase formatted sql

-- changeset sigmap:1749671959050-89

CREATE TABLE finacement (
                            realisation_id INT NOT NULL,
                            realisation_ver INT NOT NULL,
                            financement_id INT NOT NULL,
                            monnaie_id INT NULL,
                            source_de_financement_id INT NULL,
                            budget_estimer DECIMAL(15,2) NULL,
                            taux_applicable DOUBLE NULL,
                            created_at DATETIME(0) NULL,
                            created_by VARCHAR(100) NULL,
                            updated_at DATETIME(0) NULL,
                            updated_by VARCHAR(100) NULL,
                            deleted_at DATETIME(0) NULL,
                            deleted_by VARCHAR(100) NULL,
                            is_deleted TINYINT(1) NULL DEFAULT FALSE,
                            PRIMARY KEY (realisation_id, realisation_ver, financement_id)
);

-- changeset sigmap:1749671959050-90

ALTER TABLE finacement ADD CONSTRAINT FK_FINANCEMENT_TO_REALISATION
    FOREIGN KEY (realisation_id, realisation_ver) REFERENCES realisation (realisation_id, realisation_ver);

-- changeset sigmap:1749671959050-91

ALTER TABLE finacement ADD CONSTRAINT FK_FINANCEMENT_TO_MONNAIE
    FOREIGN KEY (monnaie_id) REFERENCES monnaie (monnaie_id);

-- changeset sigmap:1749671959050-92

ALTER TABLE finacement ADD CONSTRAINT FK_FINANCEMENT_TO_SOURCE_FINANCEMENT
    FOREIGN KEY (source_de_financement_id) REFERENCES ref_param (ref_param_id);
