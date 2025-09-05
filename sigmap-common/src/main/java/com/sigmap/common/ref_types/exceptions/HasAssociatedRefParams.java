package com.sigmap.common.ref_types.exceptions;

import lombok.Data;

@Data
public class HasAssociatedRefParams extends RuntimeException {
    private final Integer id;
    public HasAssociatedRefParams( String message, Integer id) {
        super(message);
        this.id = id;
    }
}
