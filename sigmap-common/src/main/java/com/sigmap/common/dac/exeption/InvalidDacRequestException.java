package com.sigmap.common.dac.exeption;

import lombok.Data;

@Data
public class InvalidDacRequestException extends RuntimeException {
    public InvalidDacRequestException(String message) {
        super(message);
    }
}
