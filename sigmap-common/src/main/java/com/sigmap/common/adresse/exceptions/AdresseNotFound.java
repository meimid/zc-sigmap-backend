package com.sigmap.common.adresse.exceptions;

import lombok.Getter;

@Getter
public class AdresseNotFound extends RuntimeException{
    private final Integer id;

    public AdresseNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
