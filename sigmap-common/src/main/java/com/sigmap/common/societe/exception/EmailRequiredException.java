package com.sigmap.common.societe.exception;

public class EmailRequiredException extends RuntimeException {
    public EmailRequiredException(String message) {
        super(message);
    }
}