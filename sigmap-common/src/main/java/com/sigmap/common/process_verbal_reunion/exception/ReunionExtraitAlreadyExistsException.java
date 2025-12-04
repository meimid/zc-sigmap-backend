package com.sigmap.common.process_verbal_reunion.exception;

import lombok.Getter;

@Getter
public class ReunionExtraitAlreadyExistsException extends RuntimeException {
    private final Integer pvId;
    private final Integer acId;

    public ReunionExtraitAlreadyExistsException(String message, Integer pvId, Integer acId) {
        super(message);
        this.pvId = pvId;
        this.acId = acId;
    }
}
