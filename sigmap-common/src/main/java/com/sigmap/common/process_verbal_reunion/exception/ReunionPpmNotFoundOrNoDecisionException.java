package com.sigmap.common.process_verbal_reunion.exception;

import lombok.Getter;

@Getter
public class ReunionPpmNotFoundOrNoDecisionException extends RuntimeException {
    private final Integer reunionId;
    private final Integer ppmId;

    public ReunionPpmNotFoundOrNoDecisionException(String message, Integer reunionId, Integer ppmId) {
        super(message);
        this.reunionId = reunionId;
        this.ppmId = ppmId;
    }
}