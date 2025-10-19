package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class PaysIsRequiredException extends RuntimeException {
    private final Integer id;

    public PaysIsRequiredException(String message, Integer id) {
        super(message);
        this.id = id;
    }
}