package com.sigmap.common.ppms.exception;

import lombok.Getter;

@Getter
public class PpmIdNotFound  extends RuntimeException {
    Integer ppmId;
    Integer ppmver;

    public PpmIdNotFound(String message,Integer ppmId) {
        super(message);
        this.ppmId = ppmId;
    }

    public PpmIdNotFound(Integer ppmId, Integer ppmVer) {

        this.ppmId = ppmId;
        this.ppmver = ppmVer;
    }
}
