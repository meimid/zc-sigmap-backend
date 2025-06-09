package com.sigmap.common.keycloakAdmin.exception;

import lombok.Getter;


@Getter
public class UserHasAssociatedCom extends RuntimeException{

    private final String nom;

    public UserHasAssociatedCom(String message, String nom) {
        super(message);
        this.nom = nom;
    }
}
