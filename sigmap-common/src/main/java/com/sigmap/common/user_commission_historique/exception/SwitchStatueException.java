package com.sigmap.common.user_commission_historique.exception;

import lombok.Getter;

@Getter
public class SwitchStatueException extends RuntimeException {
    private final String userId;

    public SwitchStatueException(String message, String userId) {
        super(message);
        this.userId = userId;
    }
}