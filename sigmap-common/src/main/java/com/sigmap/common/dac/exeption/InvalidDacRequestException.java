package com.sigmap.common.dac.exeption;

public class InvalidDacRequestException extends RuntimeException {
    public InvalidDacRequestException(String message) {
        super(message);
    }

    public InvalidDacRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
