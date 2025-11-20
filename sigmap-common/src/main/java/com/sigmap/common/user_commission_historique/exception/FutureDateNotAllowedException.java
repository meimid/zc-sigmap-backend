package com.sigmap.common.user_commission_historique.exception;

public class FutureDateNotAllowedException extends RuntimeException {
    public FutureDateNotAllowedException(String message) {
        super(message);
    }
}