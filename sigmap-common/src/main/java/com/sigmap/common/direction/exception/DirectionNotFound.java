package com.sigmap.common.direction.exception;

import lombok.Getter;

@Getter
public class DirectionNotFound extends RuntimeException {
    private final Integer directionId ;
    private final Integer acId;

    public DirectionNotFound(String message, Integer directionId, Integer acId) {
        super(message);
        this.directionId = directionId;
        this.acId = acId;
    }
}
