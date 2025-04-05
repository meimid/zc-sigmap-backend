package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class SocieteNotFound extends Exception {
    private final Integer id;
    public SocieteNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public SocieteNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}
