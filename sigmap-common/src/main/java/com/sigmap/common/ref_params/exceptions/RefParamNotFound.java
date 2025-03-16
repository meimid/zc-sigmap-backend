package com.sigmap.common.ref_params.exceptions;


import lombok.Getter;

@Getter
public class RefParamNotFound extends Exception {
    private final Integer id;

    public RefParamNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public RefParamNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}

