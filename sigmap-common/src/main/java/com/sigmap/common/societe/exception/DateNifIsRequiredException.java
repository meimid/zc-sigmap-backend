package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class DateNifIsRequiredException extends RuntimeException {
    private final Integer id;

    public DateNifIsRequiredException(String message, Integer id) {
        super(message);
        this.id = id;
    }
}