-- liquibase formatted sql

-- changeset sigmap:1757950279-1
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

-- liquibase formatted sql

-- changeset sigmap:1757950279-2

    INSERT INTO devise_historique (devise_id, code, description, taux, date_debut, date_fin, action, changed_by)
    SELECT m.monnaie_id AS devise_id, m.code, m.description, m.taux, COALESCE(m.created_at, NOW()) AS date_debut, NULL AS date_fin,'INIT' AS action, COALESCE(m.created_by, 'SYSTEM') AS changed_by
    FROM monnaie m
    WHERE m.is_deleted = FALSE AND NOT EXISTS ( SELECT 1 FROM devise_historique h  WHERE h.devise_id = m.monnaie_id );

