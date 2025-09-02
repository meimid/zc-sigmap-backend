-- liquibase formatted sql


-- changeset sigmap:1749671959050-97
CREATE SEQUENCE participant_id_seq INCREMENT BY 1 START WITH 1;

-- changeset sigmap:1749671959050-98
CREATE TABLE reunion (
                         reunion_id INT AUTO_INCREMENT NOT NULL,
                         com_id INT NOT NULL,
                         reunion_date DATETIME NULL,
                         emplacement TEXT NULL,
                         object TEXT NULL,
                         status ENUM('BROUILLON', 'NON_PLANIFIER', 'PLANIFIER', 'EN_COURS', 'VALIDER', 'CLOTURER', 'REPORTER', 'ANNULER','EN_RETARD') NULL,
                         created_at DATETIME NULL,
                         created_by VARCHAR(100) NULL,
                         updated_at DATETIME NULL,
                         updated_by VARCHAR(100) NULL,
                         deleted_at DATETIME NULL,
                         deleted_by VARCHAR(100) NULL,
                         is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                         CONSTRAINT PK_REUNION PRIMARY KEY (reunion_id)
);

-- changeset sigmap:1749671959050-99
CREATE TABLE reunion_ppm (
                             reunion_id INT NOT NULL,
                             ppm_id INT NOT NULL,
                             ppm_ver INT NOT NULL,
                             process_instance_id VARCHAR(255) NULL,
                             task_id VARCHAR(100) NULL,
                             CONSTRAINT PK_REUNION_PPM PRIMARY KEY (reunion_id, ppm_id, ppm_ver)
);

-- changeset sigmap:1749671959050-100
ALTER TABLE reunion_ppm ADD CONSTRAINT FK_REUNIONPPM_TO_REUNION
    FOREIGN KEY (reunion_id) REFERENCES reunion (reunion_id);

-- changeset sigmap:1749671959050-101
ALTER TABLE reunion_ppm ADD CONSTRAINT FK_REUNIONPPM_TO_PPM
    FOREIGN KEY (ppm_id, ppm_ver) REFERENCES ppm (ppm_id, ppm_ver);

-- changeset sigmap:1749671959050-102
CREATE TABLE reunion_dac (
                             reunion_id INT NOT NULL,
                             dac_id INT NOT NULL,
                             dac_ver INT NOT NULL,
                             process_instance_id VARCHAR(255) NULL,
                             task_id VARCHAR(100) NULL,
                             CONSTRAINT PK_REUNION_DAC PRIMARY KEY (reunion_id, dac_id, dac_ver)
);

-- changeset sigmap:1749671959050-103
ALTER TABLE reunion_dac ADD CONSTRAINT FK_REUNIONDAC_TO_REUNION
    FOREIGN KEY (reunion_id) REFERENCES reunion (reunion_id);

-- changeset sigmap:1749671959050-104
ALTER TABLE reunion_dac ADD CONSTRAINT FK_REUNIONDAC_TO_DAC
    FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver);

-- changeset sigmap:1749671959050-105
CREATE TABLE participant (
                                     reunion_id INT NOT NULL,
                                     participant_id INT NOT NULL,
                                     type ENUM('RESPONSABLE', 'MEMBRE', 'INVITE') NOT NULL DEFAULT 'INVITE',
                                     nom VARCHAR(100) NOT NULL,
                                     prenom VARCHAR(100) NOT NULL,
                                     organisme VARCHAR(100) NULL,
                                     fonction VARCHAR(100) NULL,
                                     mail VARCHAR(100) NULL,
                                     user_id VARCHAR (100) NULL,
                                     present BOOLEAN DEFAULT FALSE NOT NULL,
                                     created_at DATETIME NULL,
                                     created_by VARCHAR(100) NULL,
                                     updated_at DATETIME NULL,
                                     updated_by VARCHAR(100) NULL,
                                     deleted_at DATETIME NULL,
                                     deleted_by VARCHAR(100) NULL,
                                     is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                     CONSTRAINT PK_REUNION_PARTICIPANT PRIMARY KEY (reunion_id, participant_id)
);


-- changeset sigmap:1749671959050-106
ALTER TABLE participant ADD CONSTRAINT FK_PARTICIPANT_TO_REUNION
    FOREIGN KEY (reunion_id) REFERENCES reunion (reunion_id);
