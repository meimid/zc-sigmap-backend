package com.sigmap.common.paas.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class PaaAlreadyExistsException extends Exception {
    private final Integer id;
    private final Integer version;

    // Constructor with id, version, and message
    public PaaAlreadyExistsException(Integer id, Integer version, String message) {
        super(message);
        this.id = id;
        this.version = version;
    }

    // Constructor with id, version, message, and cause
    public PaaAlreadyExistsException(Integer id, Integer version, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
        this.version = version;
    }




}
