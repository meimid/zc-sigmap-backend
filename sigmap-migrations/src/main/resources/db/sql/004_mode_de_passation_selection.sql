-- liquibase formatted sql

-- changeset sigmap:1749671959050-4

CREATE TABLE mode_de_passation_selection (mode_de_passation_selection_id INT AUTO_INCREMENT NOT NULL, libelle VARCHAR(20) NOT NULL, categorie_id INT DEFAULT NULL NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_MODE_PASSATION_SELECTION PRIMARY KEY (mode_de_passation_selection_id));
