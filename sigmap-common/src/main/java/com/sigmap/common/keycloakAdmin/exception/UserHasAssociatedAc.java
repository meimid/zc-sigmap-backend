package com.sigmap.common.keycloakAdmin.exception;

import lombok.Getter;

@Getter
public class UserHasAssociatedAc extends RuntimeException{

    private final String nom;

    public UserHasAssociatedAc(String message, String nom) {
        super(message);
        this.nom = nom;
    }
}
