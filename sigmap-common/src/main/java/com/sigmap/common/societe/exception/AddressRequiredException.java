package com.sigmap.common.societe.exception;

public class AddressRequiredException extends RuntimeException {
    public AddressRequiredException(String message) {
        super(message);
    }
}
