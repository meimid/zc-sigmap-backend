package com.sigmap.common.ref_params.exceptions;


import lombok.Getter;

import java.util.List;
@Getter
public class ReferenceNotFound extends RuntimeException {
    private final List<String> reference;
    public ReferenceNotFound(String message, List<String> reference) {
        super(message);
        this.reference = reference;
    }
}
