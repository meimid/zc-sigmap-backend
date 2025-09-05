package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedPpm extends RuntimeException {
    private final Integer id;
    public HasAssociatedPpm(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
