package com.sigmap.common.paas.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class PaaCreationLimitExceededException extends Exception {
    private final Integer id;
    private final Integer version;
    private final int limit;

    // Constructor with id, version, limit, and message
    public PaaCreationLimitExceededException(String message, Integer id, Integer version, int limit) {
        super(message);
        this.id = id;
        this.version = version;
        this.limit = limit;
    }
}