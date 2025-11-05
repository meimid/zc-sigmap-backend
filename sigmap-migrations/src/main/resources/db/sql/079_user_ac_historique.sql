-- liquibase formatted sql

-- changeset sigmap:1757950279-3

CREATE TABLE user_ac_historique ( id INT AUTO_INCREMENT NOT NULL,
                                  user_id VARCHAR (50) NOT NULL,
                                  ac_id INT NOT NULL,
                                  date_debut TIMESTAMP NULL,
                                  date_fin TIMESTAMP NULL,
                                  status ENUM('ACTIF', 'NON_ACTIF') NOT NULL,
                                  PRIMARY KEY (id),
                                  CONSTRAINT FK_AC_TO_USER_AC_HISTORIQUE FOREIGN KEY (ac_id) REFERENCES ac (ac_id));

-- liquibase formatted sql

-- changeset sigmap:1757950279-4

INSERT INTO user_ac_historique ( user_id, ac_id, date_debut, date_fin, status )
SELECT ua.user_id, ua.ac_id, ua.created_at AS date_debut, NULL AS date_fin, 'ACTIF'  AS status
FROM user_ac ua
WHERE ua.is_deleted = FALSE;