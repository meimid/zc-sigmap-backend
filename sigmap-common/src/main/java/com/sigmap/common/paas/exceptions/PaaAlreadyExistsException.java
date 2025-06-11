package com.sigmap.common.paas.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class PaaAlreadyExistsException extends Exception {
    private final Integer id;
    private final Integer version;

    // Constructor with id, version, and message
    public PaaAlreadyExistsException(String message, Integer id, Integer version) {
        super(message);
        this.id = id;
        this.version = version;
    }





}
