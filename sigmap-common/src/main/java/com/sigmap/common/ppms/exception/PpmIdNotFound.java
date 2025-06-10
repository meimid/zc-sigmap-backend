package com.sigmap.common.ppms.exception;

import lombok.Getter;

@Getter
public class PpmIdNotFound  extends RuntimeException {
    Integer ppmId;

    public PpmIdNotFound(String message,Integer ppmId) {
        super(message);
        this.ppmId = ppmId;
    }
}
