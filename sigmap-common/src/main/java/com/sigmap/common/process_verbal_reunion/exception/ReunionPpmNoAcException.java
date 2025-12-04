package com.sigmap.common.process_verbal_reunion.exception;

import lombok.Getter;

@Getter
public class ReunionPpmNoAcException extends RuntimeException {
    private final Integer reunionId;
    private final Integer ppmId;

    public ReunionPpmNoAcException(String message, Integer reunionId, Integer ppmId) {
        super(message);
        this.reunionId = reunionId;
        this.ppmId = ppmId;
    }
}