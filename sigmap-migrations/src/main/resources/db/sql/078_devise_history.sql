-- liquibase formatted sql

-- changeset sigmap:1757950278-2
CREATE TABLE devise_historique ( id INT AUTO_INCREMENT NOT NULL,
                                 devise_id INT NOT NULL,
                                 code VARCHAR(20) NOT NULL,
                                 description VARCHAR(100) NOT NULL,
                                 taux DOUBLE (5,3) NOT NULL,
                                 date_debut TIMESTAMP NULL,
                                 date_fin TIMESTAMP NULL,
                                 action VARCHAR(20) NULL,
                                 changed_by VARCHAR(50) NOT NULL,
                                 PRIMARY KEY (id))

