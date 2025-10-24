package com.sigmap.common.keycloak_website_admin.exception;

import lombok.Getter;

@Getter
public class EmailOrUserNameAlreadyUsedForAnotherUser extends RuntimeException {

    public EmailOrUserNameAlreadyUsedForAnotherUser(String message) {
        super(message);
    }
}