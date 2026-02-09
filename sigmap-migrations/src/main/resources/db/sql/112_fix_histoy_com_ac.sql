-- liquibase formatted sql

-- changeset sigmap:17496719590113-1

INSERT INTO user_commission_historique ( user_id, commission_id, date_debut, date_fin )
    SELECT uc.user_id, uc.com_id, uc.created_at, NULL
        FROM user_com uc
            WHERE uc.is_deleted = 0
              AND NOT EXISTS ( SELECT 1
                               FROM user_commission_historique uch
                               WHERE uch.user_id = uc.user_id
                               AND uch.commission_id = uc.com_id );



-- changeset sigmap:17496719590113-2

INSERT INTO user_ac_historique ( user_id, ac_id, date_debut, date_fin )
    SELECT ua.user_id, ua.ac_id, ua.created_at, NULL
        FROM user_ac ua
            WHERE ua.is_deleted = 0
              AND NOT EXISTS ( SELECT 1
                               FROM user_ac_historique uah
                               WHERE uah.user_id = ua.user_id
                                 AND uah.ac_id = ua.ac_id );