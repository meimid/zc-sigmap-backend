package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AttributeRequire extends RuntimeException{

    private final String nom;

    public AttributeRequire(String message, String nom) {
        super(message);
        this.nom = nom;
    }
}
