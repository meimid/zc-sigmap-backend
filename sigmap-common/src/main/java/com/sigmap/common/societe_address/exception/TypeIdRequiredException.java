package com.sigmap.common.societe_address.exception;

public class TypeIdRequiredException extends RuntimeException {
    public TypeIdRequiredException() {
        super("Type ID is required.");
    }

    public TypeIdRequiredException(String message) {
        super(message);
    }
}