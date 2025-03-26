package com.sigmap.common.agpm.exceptions;


import lombok.Getter;

@Getter
public class AgpmNotFound extends RuntimeException {

    private final Integer id;

    public AgpmNotFound(String message, Integer id) {

        super(message);
        this.id = id;
    }

    public AgpmNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}
