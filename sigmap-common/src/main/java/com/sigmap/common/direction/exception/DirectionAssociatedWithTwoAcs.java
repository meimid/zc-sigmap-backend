package com.sigmap.common.direction.exception;

import lombok.Getter;

@Getter
public class DirectionAssociatedWithTwoAcs extends RuntimeException {

    public DirectionAssociatedWithTwoAcs(String message) {
        super(message);
    }
}
