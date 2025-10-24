package com.sigmap.common.keycloak_website_admin.exception;

import lombok.Getter;

@Getter
public class CreateUserFailed extends RuntimeException {

    public CreateUserFailed(String message) {
        super(message);
    }
}
