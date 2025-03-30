package com.sigmap.common.paas.exceptions;

import org.springframework.http.HttpStatus;

public class PaaNotFound extends Exception {
    private final Integer id;

    public PaaNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public PaaNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }

    public PaaNotFound(HttpStatus httpStatus, Integer id) {

        this.id = id;
    }
}
