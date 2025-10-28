package com.sigmap.common.user_societe.exception;

import lombok.Getter;

@Getter
public class UserNotFoundInKeycloak extends RuntimeException {

    private final String userId;

    public UserNotFoundInKeycloak(String message,String userId) {
        super(message);
        this.userId = userId;
    }

}