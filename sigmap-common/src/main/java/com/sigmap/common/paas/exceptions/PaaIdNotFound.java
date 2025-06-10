package com.sigmap.common.paas.exceptions;

import lombok.Getter;

@Getter
public class PaaIdNotFound extends RuntimeException {
    Integer paaId;

    public PaaIdNotFound(String message,Integer paaId) {
        super(message);
        this.paaId = paaId;
    }
}
