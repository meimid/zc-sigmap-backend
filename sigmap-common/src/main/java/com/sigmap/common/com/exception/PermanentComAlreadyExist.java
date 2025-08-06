package com.sigmap.common.com.exception;

import lombok.Getter;

@Getter
public class PermanentComAlreadyExist extends RuntimeException {
    private final String code;

    public PermanentComAlreadyExist(String message, String code) {
        super(message);
        this.code = code;
    }
}