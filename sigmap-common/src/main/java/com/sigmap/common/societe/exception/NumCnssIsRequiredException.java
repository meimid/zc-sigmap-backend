package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class NumCnssIsRequiredException extends RuntimeException {
    private final Integer id;

    public NumCnssIsRequiredException(String message, Integer id) {
        super(message);
        this.id = id;
    }
}