-- liquibase formatted sql

-- changeset sigmap:1757950279-1

CREATE TABLE IF NOT EXISTS user_commission_historique (
    id INT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(255) NOT NULL,
    commission_id INT NOT NULL,
    date_debut DATETIME NOT NULL,
    date_fin DATETIME DEFAULT NULL,
    status ENUM('ACTIF', 'NON_ACTIF') NOT NULL DEFAULT 'NON_ACTIF',
    CONSTRAINT pk_user_commission_historique PRIMARY KEY (id));

-- changeset sigmap:1757950279-2

ALTER TABLE user_commission_historique
    ADD CONSTRAINT fk_user_com_hist
        FOREIGN KEY (user_id, commission_id)
            REFERENCES user_com (user_id, com_id);