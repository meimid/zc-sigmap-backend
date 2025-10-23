package com.sigmap.common.user_societe.exception;

import lombok.Getter;

@Getter
public class CantResponsableNonActif extends RuntimeException {
    private final String userId;

    public CantResponsableNonActif(String message, String userId) {
        super(message);
        this.userId = userId;
    }
}