package com.sigmap.common.user_societe.exception;

public class SocieteIdRequiredException extends RuntimeException {
    public SocieteIdRequiredException() {
        super("Societe ID is required.");
    }

    public SocieteIdRequiredException(String message) {
        super(message);
    }
}
