-- liquibase formatted sql

-- changeset sigmap:1757950270-1

ALTER TABLE societe
    ADD COLUMN nif VARCHAR(255) NOT NULL,
    ADD COLUMN date_nif DATETIME NOT NULL,
    ADD COLUMN num_cnss INTEGER NOT NULL,
    ADD COLUMN status ENUM('VALIDEE', 'REJETEE','EN_COURS') NOT NULL DEFAULT 'EN_COURS';

-- changeset sigmap:1757950270-2
CREATE TABLE societe_activite (
    societe_id INT NOT NULL,
    activite_id INT NOT NULL,
    PRIMARY KEY (societe_id, activite_id),
    CONSTRAINT FK_SOCIETE_TO_SOCIETE_ACTIVITE
      FOREIGN KEY (societe_id) REFERENCES societe(societe_id),
    CONSTRAINT FK_ACTIVITE_TO_SOCIETE_ACTIVITE
      FOREIGN KEY (activite_id) REFERENCES activites(activite_id)
);

-- liquibase formatted sql

-- changeset sigmap:1757950270-3
CREATE TABLE IF NOT EXISTS user_societe (
    user_id VARCHAR(255) NOT NULL,
    societe_id INT NOT NULL,
    responsable TINYINT(1) NOT NULL DEFAULT 0,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    num_cin VARCHAR(255),
    date_delivrance_cin DATETIME,
    tel1 VARCHAR(255),
    tel2 VARCHAR(255),
    email VARCHAR(255),
    status ENUM('ACTIF', 'NON_ACTIF') NOT NULL DEFAULT 'NON_ACTIF',
    created_at DATETIME DEFAULT NULL,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by VARCHAR(100) DEFAULT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, societe_id),
    CONSTRAINT fk_user_societe_societe
    FOREIGN KEY (societe_id) REFERENCES societe(societe_id)
    );


-- changeset sigmap:1757950270-4
ALTER TABLE societe
DROP COLUMN IF EXISTS pays_id,
    DROP COLUMN IF EXISTS nni;

 -- changeset sigmap:1757950270-5

ALTER TABLE societe_address MODIFY societe_address_id INT;

-- changeset sigmap:1757950270-6

ALTER TABLE societe_address DROP PRIMARY KEY;

-- changeset sigmap:1757950270-7

ALTER TABLE societe_address DROP COLUMN societe_address_id;


-- changeset sigmap:1757950270-8
ALTER TABLE societe_address
    DROP COLUMN IF EXISTS societe_adresse_id,
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS created_by,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS updated_by,
    DROP COLUMN IF EXISTS deleted_at,
    DROP COLUMN IF EXISTS deleted_by,
    DROP COLUMN IF EXISTS is_deleted,
    DROP COLUMN IF EXISTS address;

-- changeset sigmap:1757950270-9

ALTER TABLE societe_address ADD COLUMN adresse_id INT NOT NULL;


-- changeset sigmap:1757950270-10
ALTER TABLE societe_address
    ADD CONSTRAINT fk_societe_address_societe
        FOREIGN KEY (societe_id) REFERENCES societe(societe_id),
    ADD CONSTRAINT fk_societe_address_adresse
        FOREIGN KEY (adresse_id) REFERENCES adresse(adresse_id);

-- changeset sigmap:1757950270-11

ALTER TABLE societe_address
    MODIFY COLUMN societe_id INT NOT NULL FIRST,
    MODIFY COLUMN adresse_id INT NOT NULL AFTER societe_id;

-- changeset sigmap:1757950270-12
ALTER TABLE societe_address
    ADD PRIMARY KEY (societe_id, adresse_id);




