package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedPaa extends RuntimeException {
    private final Integer id;
    public HasAssociatedPaa(String message, Integer id) {
        super(message);
        this.id = id;
    }
}