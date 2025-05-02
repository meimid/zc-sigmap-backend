package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;
import java.util.List;

@Getter
public class RefParamNotFound extends Exception {
    private final List<Integer> id;
    public RefParamNotFound( String message , List<Integer> id) {
        super(message);
        this.id = id;
    }
    public RefParamNotFound(String message, Throwable cause, List<Integer> id) {
        super(message, cause);
        this.id = id;
    }

}

