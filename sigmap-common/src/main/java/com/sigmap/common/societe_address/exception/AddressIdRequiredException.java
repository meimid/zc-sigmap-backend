package com.sigmap.common.societe_address.exception;

public class AddressIdRequiredException extends RuntimeException {
    public AddressIdRequiredException() {
        super("Address ID is required.");
    }

    public AddressIdRequiredException(String message) {
        super(message);
    }
}