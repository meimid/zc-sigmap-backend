package com.sigmap.common.ac.exception;

import lombok.Data;

@Data
public class AcHasAssociatedCom extends RuntimeException {
    private final Integer id;

    public AcHasAssociatedCom(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
