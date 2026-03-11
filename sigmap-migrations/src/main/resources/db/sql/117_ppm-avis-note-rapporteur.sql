-- liquibase formatted sql

-- changeset sigmap:17496719590118-1
-- liquibase formatted sql

-- changeset sigmap:20260311-ppm-avis-note-rapporteur-1
CREATE TABLE ppm_avis_note_rapporteur (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          process_instance_id VARCHAR(255) NOT NULL,
                                          task_id VARCHAR(255) NOT NULL,
                                          ppm_id INT NOT NULL,
                                          ppm_ver INT NOT NULL,
                                          rapporteur_user_id VARCHAR(255) NOT NULL,
                                          avis TEXT NULL,
                                          doc_id_note INT NULL,

                                          created_at DATETIME NULL,
                                          created_by VARCHAR(255) NULL,
                                          updated_at DATETIME NULL,
                                          updated_by VARCHAR(255) NULL,
                                          deleted_at DATETIME NULL,
                                          deleted_by VARCHAR(255) NULL,
                                          is_deleted TINYINT(1) DEFAULT 0

);