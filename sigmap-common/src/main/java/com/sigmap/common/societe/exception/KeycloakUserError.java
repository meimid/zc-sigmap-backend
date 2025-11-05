package com.sigmap.common.societe.exception;


public class KeycloakUserError extends RuntimeException {

    private final String details;

    public KeycloakUserError(String details) {
        super(details);
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}

