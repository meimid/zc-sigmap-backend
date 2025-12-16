-- liquibase formatted sql

-- changeset sigmap:1749671959098-1
CREATE TABLE appel_offre(
    appel_offre_id     INT AUTO_INCREMENT NOT NULL,
    ppm_id             INT NOT NULL,
    ppm_ver            INT NOT NULL,
    realisation_id     INT NOT NULL,
    realisation_ver    INT NOT NULL,

    reference          VARCHAR(255),
    nom                VARCHAR(255),
    description        VARCHAR(255),
    etape              VARCHAR(255),

    status_appel_offre ENUM('CREE', 'REJETER', 'CLOTURER') DEFAULT 'CREE' NOT NULL,
    lot_separes        TINYINT(1) DEFAULT NULL NULL,

    created_at datetime DEFAULT NULL NULL,
    created_by VARCHAR(100) NULL,
    updated_at datetime DEFAULT NULL NULL,
    updated_by VARCHAR(100) NULL,
    deleted_at datetime DEFAULT NULL NULL,
    deleted_by VARCHAR(100) NULL,
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_APPEL_OFFRE PRIMARY KEY (appel_offre_id)
);


-- changeset sigmap:1749671959098-2
ALTER TABLE appel_offre ADD CONSTRAINT fk_appel_offre_ppm_id_ver FOREIGN KEY (ppm_id,ppm_ver)
    REFERENCES ppm (ppm_id,ppm_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;


-- changeset sigmap:1749671959098-3
ALTER TABLE appel_offre ADD CONSTRAINT fk_realisation_id_ver FOREIGN KEY (realisation_id,realisation_ver)
    REFERENCES realisation (realisation_id,realisation_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;



