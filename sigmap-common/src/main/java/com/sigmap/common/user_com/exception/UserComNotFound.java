package com.sigmap.common.user_com.exception;

import lombok.Getter;

@Getter
public class UserComNotFound extends RuntimeException {
    private final String userId;
    private final Integer comId;
    public UserComNotFound( String message, String userId, Integer comId) {
        super(message);
        this.userId = userId;
        this.comId = comId;
    }
}
