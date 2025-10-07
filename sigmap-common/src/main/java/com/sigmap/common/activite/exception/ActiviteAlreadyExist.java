package com.sigmap.common.activite.exception;

import lombok.Getter;

@Getter
public class ActiviteAlreadyExist  extends RuntimeException{
    private final String libelle;

    public ActiviteAlreadyExist(String message, String libelle) {
        super(message);
        this.libelle = libelle;
    }
}
