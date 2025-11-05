package com.sigmap.common.user_societe.exception;

public class TokenRequired extends RuntimeException {

    public TokenRequired() {
        super("A valid authentication token is required to access this resource.");
    }

    public TokenRequired(String message) {
        super(message);
    }
}