package com.sigmap.common.ref_params.exceptions;

public class RefParamNotFound extends Exception {
    public RefParamNotFound( String message) {
        super(message);
    }
    public RefParamNotFound( String message, Throwable cause) {
        super(message, cause);
    }
}

