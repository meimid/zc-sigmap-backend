package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcHasAssociatedPpm extends RuntimeException {
    private final Integer id;

    public AcHasAssociatedPpm(String message, Integer id) {
        super(message);
        this.id = id;
    }
}