package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class RefParamDeleted extends RuntimeException {
    private final Integer id;

    public RefParamDeleted(String message, Integer id) {
        super(message);
        this.id = id;
    }
}