package com.sigmap.common.user_societe.exception;

import lombok.Getter;

@Getter
public class UserSocieteNotFound extends RuntimeException {
    Integer societeId;
    String userId;


    public UserSocieteNotFound(String message,String userId, Integer societeId) {
        super(message);
        this.userId = userId;
        this.societeId = societeId;
    }

    public UserSocieteNotFound(String userId) {
        this.userId = userId;
    }
}
