package com.sigmap.common.paas.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class PaaNotFound extends Exception {
    private final Integer id;
    private final Integer version;

    public PaaNotFound(Integer id, Integer version, String message) {
        super(message);
        this.id = id;
        this.version = version;
    }

    public PaaNotFound(Integer id, Integer version, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
        this.version = version;
    }


}
