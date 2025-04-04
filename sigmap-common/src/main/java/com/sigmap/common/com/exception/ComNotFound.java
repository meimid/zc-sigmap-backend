package com.sigmap.common.com.exception;

import lombok.Getter;

@Getter
public class ComNotFound extends Exception{
    private final Integer id;
    public ComNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public ComNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}
