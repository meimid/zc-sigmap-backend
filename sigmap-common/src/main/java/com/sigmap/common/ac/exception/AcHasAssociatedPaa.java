package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcHasAssociatedPaa extends RuntimeException {
    private final Integer id;

    public AcHasAssociatedPaa(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
