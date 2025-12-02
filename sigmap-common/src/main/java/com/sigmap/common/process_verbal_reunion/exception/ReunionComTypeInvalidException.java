package com.sigmap.common.process_verbal_reunion.exception;

import lombok.Getter;

@Getter
public class ReunionComTypeInvalidException extends RuntimeException {
    private final Integer reunionId;
    private final String comTypeCode;

    public ReunionComTypeInvalidException(String message, Integer reunionId, String comTypeCode) {
        super(message);
        this.reunionId = reunionId;
        this.comTypeCode = comTypeCode;
    }
}