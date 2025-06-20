package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class RefParamHasAssociatedAc extends RuntimeException {
    private final Integer id;
    public RefParamHasAssociatedAc(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
