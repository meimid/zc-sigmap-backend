-- liquibase formatted sql

-- changeset sigmap:1749671959094-1

CREATE TABLE seuil_general ( seuil_id INT AUTO_INCREMENT NOT NULL,
                             type_marcher_id INT NOT NULL,
                             montant Decimal(10,3),
                             created_at datetime DEFAULT NULL NULL,
                             created_by VARCHAR(100) NULL,
                             updated_at datetime DEFAULT NULL NULL,
                             updated_by VARCHAR(100) NULL,
                             deleted_at datetime DEFAULT NULL NULL,
                             deleted_by VARCHAR(100) NULL,
                             is_deleted TINYINT(1) DEFAULT 0 NOT NULL,
                             CONSTRAINT PK_SEUIL_GENERAL PRIMARY KEY (seuil_id));


-- changeset sigmap:1749671959094-2
ALTER TABLE seuil_general ADD CONSTRAINT FK_SEUIL_GENERAL_TO_TYPE_MARCHER
    FOREIGN KEY (type_marcher_id) REFERENCES ref_param (ref_param_id);
