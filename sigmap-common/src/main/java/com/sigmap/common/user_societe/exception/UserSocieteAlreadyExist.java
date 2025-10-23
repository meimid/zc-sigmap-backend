package com.sigmap.common.user_societe.exception;

import lombok.Getter;

@Getter
public class UserSocieteAlreadyExist extends RuntimeException {
    Integer societeId;
    String userId;


    public UserSocieteAlreadyExist(String message,String userId, Integer societeId) {
        super(message);
        this.userId = userId;
        this.societeId = societeId;
    }
}


