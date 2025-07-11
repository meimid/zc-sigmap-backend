package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcAttributeRequiredAndValid extends RuntimeException{

    private final String nom;

    public AcAttributeRequiredAndValid(String message, String nom) {
        super(message);
        this.nom = nom;
    }
}
