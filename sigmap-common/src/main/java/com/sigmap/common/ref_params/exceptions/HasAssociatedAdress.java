package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedAdress extends RuntimeException {
    private final Integer id;
    public HasAssociatedAdress(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
