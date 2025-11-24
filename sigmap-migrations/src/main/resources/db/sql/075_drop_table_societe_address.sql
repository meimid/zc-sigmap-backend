-- liquibase formatted sql

-- changeset sigmap:1757950276-1
ALTER TABLE societe
    ADD COLUMN adresse_id INT NOT NULL,
    MODIFY date_nif DATETIME NULL,
    MODIFY num_cnss VARCHAR(255) NOT NULL;

-- changeset sigmap:1757950276-2
ALTER TABLE societe
    ADD CONSTRAINT fk_societe_adresse
        FOREIGN KEY (adresse_id) REFERENCES adresse(adresse_id);

-- changeset sigmap:1757950276-3
DROP TABLE IF EXISTS societe_address;

