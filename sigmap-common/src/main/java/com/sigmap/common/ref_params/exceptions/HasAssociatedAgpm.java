package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedAgpm extends RuntimeException {
    private final Integer id;
    public HasAssociatedAgpm(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
