package com.sigmap.common.type_agrement.exception;

import lombok.Getter;

@Getter
public class TypeAgrementAlreadyExist extends RuntimeException{
    private final String libelle;

    public TypeAgrementAlreadyExist(String message, String libelle) {
        super(message);
        this.libelle = libelle;
    }
}
