package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedRefRealisation extends RuntimeException {
    private final Integer id;
    public HasAssociatedRefRealisation(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
