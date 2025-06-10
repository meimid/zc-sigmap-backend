package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcAttributeRequire extends RuntimeException{

    private final String nom;

    public AcAttributeRequire(String message, String nom) {
        super(message);
        this.nom = nom;
    }
}
