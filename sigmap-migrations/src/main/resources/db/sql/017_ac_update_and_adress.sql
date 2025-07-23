-- liquibase formatted sql

-- changeset sigmap:1749671959050-36
CREATE TABLE adresse (
                         adresse_id INT AUTO_INCREMENT NOT NULL,
                         email VARCHAR(255) NULL,
                         tel1 VARCHAR(255) NULL,
                         tel2 VARCHAR(255) NULL,
                         fax VARCHAR(255) NULL,
                         adresse TEXT  NULL,
                         comp_adresse TEXT  NULL,
                         code_postal VARCHAR(50) NULL,
                         ville VARCHAR(50) NULL,
                         pays_id INT NULL,
                         created_at DATETIME NULL,
                         created_by VARCHAR(100) NULL,
                         updated_at DATETIME NULL,
                         updated_by VARCHAR(100) NULL,
                         deleted_at DATETIME NULL,
                         deleted_by VARCHAR(100) NULL,
                         is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                         CONSTRAINT PK_ADRESSE PRIMARY KEY (adresse_id)
);

-- changeset sigmap:1749671959050-37
ALTER TABLE ac ADD COLUMN is_tutel BOOLEAN DEFAULT FALSE NOT NULL;

-- changeset sigmap:1749671959050-38
ALTER TABLE ac ADD COLUMN logo VARCHAR(100) NULL;

-- changeset sigmap:1749671959050-39
ALTER TABLE ac ADD COLUMN adresse_id INT NULL;

-- changeset sigmap:1749671959050-40
ALTER TABLE ac ADD COLUMN com_id INT NULL;

-- changeset sigmap:1749671959050-41
ALTER TABLE ac ADD CONSTRAINT FK_AC_TO_ADRESSE
    FOREIGN KEY (adresse_id) REFERENCES adresse (adresse_id);

-- changeset sigmap:1749671959050-42
ALTER TABLE ac ADD CONSTRAINT FK_AC_TO_COM
    FOREIGN KEY (com_id) REFERENCES com (com_id);


