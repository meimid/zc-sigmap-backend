package com.sigmap.common.user_commission_historique.exception;

import lombok.Getter;

@Getter
public class UserCommissionHistoriqueNotFound extends RuntimeException {
        private final Integer id;
    public UserCommissionHistoriqueNotFound( String message, Integer id) {
            super(message);
            this.id = id;
        }
}
