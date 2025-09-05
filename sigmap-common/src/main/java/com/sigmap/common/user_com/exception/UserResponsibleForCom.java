package com.sigmap.common.user_com.exception;

import lombok.Getter;

@Getter
public class UserResponsibleForCom extends RuntimeException {
    private final String userId;
    private final Integer comId;
    public UserResponsibleForCom(String message, String userId, Integer comId) {
        super(message);
        this.userId = userId;
        this.comId = comId;
    }
}
