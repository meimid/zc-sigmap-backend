package com.sigmap.common.societe_address.exception;

public class SocieteAddressAlreadyExist extends RuntimeException {

    public SocieteAddressAlreadyExist() {
        super("SocieteAddress already exists.");
    }

    public SocieteAddressAlreadyExist(String message) {
        super(message);
    }
}
