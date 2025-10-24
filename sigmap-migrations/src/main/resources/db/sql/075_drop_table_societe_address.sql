-- liquibase formatted sql

-- changeset sigmap:1757950271-1
ALTER TABLE societe
    ADD COLUMN address_id INT NOT NULL,
    MODIFY date_nif DATETIME NULL,
    MODIFY num_cnss VARCHAR(255) NOT NULL;

-- changeset sigmap:1757950271-2
ALTER TABLE societe
    ADD CONSTRAINT fk_societe_adresse
        FOREIGN KEY (address_id) REFERENCES adresse(adresse_id);

-- changeset sigmap:1757950271-3
DROP TABLE IF EXISTS societe_address;

