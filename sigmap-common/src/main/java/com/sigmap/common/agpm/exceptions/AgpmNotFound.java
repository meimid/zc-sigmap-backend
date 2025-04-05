package com.sigmap.common.agpm.exceptions;

import lombok.Data;

@Data
public class AgpmNotFound extends Exception {
    private final Integer id;
    private final Integer version;

    public AgpmNotFound(Integer id, Integer version, String message) {
        super(message);
        this.id = id;
        this.version = version;
    }

    public AgpmNotFound(Integer id, Integer version, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
        this.version = version;
    }
}