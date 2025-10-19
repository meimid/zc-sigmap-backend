package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class NifIsRequiredException extends RuntimeException {
    private final Integer id;

    public NifIsRequiredException(String message, Integer id) {
        super(message);
        this.id = id;
    }
}