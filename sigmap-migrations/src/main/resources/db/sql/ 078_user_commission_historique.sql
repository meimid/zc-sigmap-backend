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

-- changeset sigmap:1757950279-3
INSERT INTO user_commission_historique (
    user_id,
    commission_id,
    date_debut,
    date_fin,
    status
)
SELECT
    uc.user_id,
    uc.com_id,
    uc.created_at AS date_debut,
    CASE
        WHEN uc.is_deleted = 1 THEN uc.deleted_at
        ELSE NULL
        END AS date_fin,
    CASE
        WHEN uc.is_deleted = 0 THEN 'ACTIF'
        ELSE 'NON_ACTIF'
        END AS status
FROM user_com uc
WHERE NOT EXISTS (
    SELECT 1
    FROM user_commission_historique h
    WHERE h.user_id = uc.user_id
      AND h.commission_id = uc.com_id
);
