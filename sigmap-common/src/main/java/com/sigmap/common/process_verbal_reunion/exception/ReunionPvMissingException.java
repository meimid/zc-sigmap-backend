package com.sigmap.common.process_verbal_reunion.exception;

import lombok.Getter;

@Getter
public class ReunionPvMissingException extends RuntimeException {
    private final Integer reunionId;

    public ReunionPvMissingException(String message, Integer reunionId) {
        super(message);
        this.reunionId = reunionId;
    }
}