package com.sigmap.common.ac.exception;

import lombok.Data;

@Data
public class AcHasAssociatedAgpm extends RuntimeException {
    private final Integer id;

    public AcHasAssociatedAgpm(String message, Integer id) {
        super(message);
        this.id = id;
    }
}