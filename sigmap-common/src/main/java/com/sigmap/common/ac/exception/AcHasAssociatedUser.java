package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcHasAssociatedUser extends RuntimeException {
    private final Integer id;
    public AcHasAssociatedUser(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
