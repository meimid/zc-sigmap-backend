package com.sigmap.common.com.exception;

import lombok.Getter;

@Getter
public class ComHasAssociatedUser extends RuntimeException {
    private final Integer id;
    public ComHasAssociatedUser(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
