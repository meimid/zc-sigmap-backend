package com.sigmap.common.com.exception;

import lombok.Getter;

@Getter
public class UserNotFound extends RuntimeException {
    private final String userId;

    public UserNotFound(String message, String userId) {
        super(message);
        this.userId = userId;
    }
}