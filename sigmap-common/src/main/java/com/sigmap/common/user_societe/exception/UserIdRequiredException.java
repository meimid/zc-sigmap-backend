package com.sigmap.common.user_societe.exception;

public class UserIdRequiredException extends RuntimeException {
    public UserIdRequiredException() {
        super("User ID is required.");
    }

    public UserIdRequiredException(String message) {
        super(message);
    }
}
