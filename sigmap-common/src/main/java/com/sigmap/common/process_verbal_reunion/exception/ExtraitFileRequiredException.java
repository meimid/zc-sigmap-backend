package com.sigmap.common.process_verbal_reunion.exception;

import lombok.Getter;

@Getter
public class ExtraitFileRequiredException extends RuntimeException {
    private final Integer reunionId;

    public ExtraitFileRequiredException(String message, Integer reunionId) {
        super(message);
        this.reunionId = reunionId;
    }
}