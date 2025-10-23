package com.sigmap.common.user_societe.exception;

public class UserAlreadyRegisteredToOtherCompany extends RuntimeException {

    public UserAlreadyRegisteredToOtherCompany(String message ) {
        super(message);

    }
}