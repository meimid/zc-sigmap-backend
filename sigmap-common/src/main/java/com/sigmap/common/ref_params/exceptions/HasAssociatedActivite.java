package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedActivite extends RuntimeException {
    private final Integer id;
    public HasAssociatedActivite(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
