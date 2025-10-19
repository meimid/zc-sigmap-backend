package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class ActiviteIsRequiredException extends RuntimeException {
    private final Integer id;

    public ActiviteIsRequiredException(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
