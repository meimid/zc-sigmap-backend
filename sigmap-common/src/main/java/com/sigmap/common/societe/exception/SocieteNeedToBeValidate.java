package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class SocieteNeedToBeValidate extends RuntimeException {
    private final Integer id;

    public SocieteNeedToBeValidate(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
