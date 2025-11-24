package com.sigmap.common.activite.exception;

import lombok.Getter;

@Getter
public class ActiviteNotFound extends RuntimeException{
    private final Integer id;

    public ActiviteNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
