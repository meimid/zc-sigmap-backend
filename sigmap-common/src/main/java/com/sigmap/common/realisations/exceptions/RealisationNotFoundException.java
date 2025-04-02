package com.sigmap.common.realisations.exceptions;

import lombok.Data;

@Data
public class RealisationNotFoundException extends Exception {

    private final Integer id;
    private final Integer version;

    public RealisationNotFoundException(Integer id, Integer version, String message) {
        super(message);
        this.id = id;
        this.version = version;
    }

    public RealisationNotFoundException(Integer id, Integer version, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
        this.version = version;
    }
}