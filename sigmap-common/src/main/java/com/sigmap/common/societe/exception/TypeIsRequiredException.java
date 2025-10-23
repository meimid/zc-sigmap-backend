package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class TypeIsRequiredException extends RuntimeException {
    private final Integer id;

    public TypeIsRequiredException(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
