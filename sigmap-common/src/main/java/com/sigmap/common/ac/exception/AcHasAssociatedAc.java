package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcHasAssociatedAc extends RuntimeException {
    private final Integer id;

    public AcHasAssociatedAc(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
