
-- liquibase formatted sql

-- changeset sigmap:1749671959050-11

CREATE SEQUENCE pv_membre_present_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

CREATE TABLE process_verbal (
                                ppm_id INT NOT NULL,
                                ppm_ver INT NOT NULL,
                                process_verbal_id VARCHAR(255) NOT NULL,
                                date_debut_reunion DATETIME NULL,
                                observation VARCHAR(255) NULL,
                                decision_id TINYINT(1) DEFAULT 0 NOT NULL,
                                type_id INT NOT NULL,
                                status ENUM('BROUILLON', 'EN_TRAITEMENT', 'PUBLIE', 'VALIDE') NOT NULL DEFAULT 'BROUILLON',
                                created_at datetime DEFAULT NULL NULL,
                                created_by VARCHAR(100) NULL,
                                updated_at datetime DEFAULT NULL NULL,
                                updated_by VARCHAR(100) NULL,
                                deleted_at datetime DEFAULT NULL NULL,
                                deleted_by VARCHAR(100) NULL,
                                is_deleted TINYINT(1) DEFAULT 0 NOT NULL,
                                CONSTRAINT PK_PROCESS_VERBAL PRIMARY KEY (ppm_id, ppm_ver, process_verbal_id)
);

-- changeset sigmap:1749671959050-12
CREATE TABLE pv_docs (
                         ppm_id INT NOT NULL,
                         ppm_ver INT NOT NULL,
                         process_verbal_id VARCHAR(255) NOT NULL,
                         doc_id INT NOT NULL,
                         pv TINYINT(1) DEFAULT 0 NOT NULL,
                         CONSTRAINT PK_PV_DOCS PRIMARY KEY (ppm_id, ppm_ver, process_verbal_id, doc_id)
);

-- changeset sigmap:1749671959050-13
CREATE TABLE pv_membre_present (
                                   ppm_id INT NOT NULL,
                                   ppm_ver INT NOT NULL,
                                   process_verbal_id VARCHAR(255) NOT NULL,
                                   pv_membre_present_id INT NOT NULL,
                                   nom VARCHAR(255) NULL,
                                   prenom VARCHAR(255) NULL,
                                   present TINYINT(1) NOT NULL DEFAULT 0,
                                   fonction_id INT NOT NULL,
                                   user_id VARCHAR(255) NULL,
                                   created_at datetime DEFAULT NULL NULL,
                                   created_by VARCHAR(100) NULL,
                                   updated_at datetime DEFAULT NULL NULL,
                                   updated_by VARCHAR(100) NULL,
                                   deleted_at datetime DEFAULT NULL NULL,
                                   deleted_by VARCHAR(100) NULL,
                                   is_deleted TINYINT(1) DEFAULT 0 NOT NULL,
                                   CONSTRAINT PK_MEMBRE_PRESENT PRIMARY KEY (ppm_id, ppm_ver, process_verbal_id, pv_membre_present_id)
);