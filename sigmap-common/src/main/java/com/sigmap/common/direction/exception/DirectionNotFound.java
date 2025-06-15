package com.sigmap.common.direction.exception;

import lombok.Getter;

@Getter
public class DirectionNotFound extends RuntimeException {
    private final Integer id ;

    public DirectionNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
