package com.sigmap.common.process_verbal_reunion.exception;

import lombok.Getter;

@Getter
public class ReunionAcNotEligibleForExtraitException extends RuntimeException {
    private final Integer reunionId;
    private final Integer acId;

    public ReunionAcNotEligibleForExtraitException(String message, Integer reunionId, Integer acId) {
        super(message);
        this.reunionId = reunionId;
        this.acId = acId;
    }
}