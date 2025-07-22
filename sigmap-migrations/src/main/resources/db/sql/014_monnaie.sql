-- liquibase formatted sql

-- changeset sigmap:1749671959050-28
CREATE TABLE monnaie (monnaie_id INT AUTO_INCREMENT NOT NULL, code VARCHAR(20) NOT NULL, description VARCHAR(50) NOT NULL, taux DOUBLE PRECISION NOT NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_MONNAIE PRIMARY KEY (monnaie_id));

