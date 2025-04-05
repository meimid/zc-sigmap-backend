package com.sigmap.common.realisations.exceptions;

import lombok.Data;

@Data
public class RealisationAlreadyExistsException extends Exception{

    private final Integer id;
    private final Integer version;

    public RealisationAlreadyExistsException(Integer id, Integer version, String message) {
        super(message);
        this.id = id;
        this.version = version;
    }

    public RealisationAlreadyExistsException(Integer id, Integer version, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
        this.version = version;
    }


}
