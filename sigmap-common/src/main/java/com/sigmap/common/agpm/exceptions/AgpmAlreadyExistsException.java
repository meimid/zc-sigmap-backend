package com.sigmap.common.agpm.exceptions;

import lombok.Data;

@Data
public class AgpmAlreadyExistsException extends Exception {
    private final Integer id;
    private final Integer version;

    public AgpmAlreadyExistsException(String message, Integer id, Integer version) {
        super(message);
        this.id = id;
        this.version = version;
    }

}