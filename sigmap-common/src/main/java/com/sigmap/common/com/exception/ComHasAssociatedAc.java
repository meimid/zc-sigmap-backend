package com.sigmap.common.com.exception;

import lombok.Data;

@Data
public class ComHasAssociatedAc extends RuntimeException {
    private final Integer id;
    public ComHasAssociatedAc(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
