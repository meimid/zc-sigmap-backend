package com.sigmap.common.monnaie.exceptions;

import lombok.Getter;

@Getter
public class MonnaieCodeAlreadyExist extends RuntimeException {
    private final String code;

    public MonnaieCodeAlreadyExist(String message, String code) {
        super(message);
        this.code = code;
    }
}