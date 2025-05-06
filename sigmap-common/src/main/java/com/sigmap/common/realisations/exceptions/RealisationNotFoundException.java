package com.sigmap.common.realisations.exceptions;


import lombok.Getter;

@Getter
public class RealisationNotFoundException extends Exception {

    private final Integer id;
    private final Integer version;

    public RealisationNotFoundException(String message, Integer id, Integer version ) {
        super(message);
        this.id = id;
        this.version = version;
    }

}