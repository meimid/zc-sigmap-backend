package com.sigmap.common.keycloakAdmin.exception;

public class UserDoesNotHavePermission extends RuntimeException{


    public UserDoesNotHavePermission(String message) {
        super(message);
    }
}
