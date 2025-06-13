package com.sigmap.common.direction.exception;

import lombok.Getter;

@Getter
public class DirectionNotBelongToAc extends RuntimeException{
    private Integer id;
    private Integer acId;
    public DirectionNotBelongToAc(String message, Integer id, Integer acId) {
        super(message);
        this.id = id;
        this.acId = acId;

    }
}
