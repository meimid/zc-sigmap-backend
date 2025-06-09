package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcNomAlreadyExist extends RuntimeException{

    private final String nom;

    public AcNomAlreadyExist(String message, String nom) {
        super(message);
        this.nom = nom;
    }
}
