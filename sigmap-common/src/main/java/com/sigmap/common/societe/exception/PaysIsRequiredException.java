package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class PaysIsRequiredException extends RuntimeException {

    public PaysIsRequiredException(String message) {
        super(message);
    }
}