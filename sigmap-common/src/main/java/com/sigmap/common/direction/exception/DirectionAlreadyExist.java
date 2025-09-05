package com.sigmap.common.direction.exception;

import lombok.Getter;

@Getter
public class DirectionAlreadyExist extends RuntimeException {
    private final String libelle ;


    public DirectionAlreadyExist(String message, String libelle) {
        super(message);
        this.libelle= libelle;
    }
}
