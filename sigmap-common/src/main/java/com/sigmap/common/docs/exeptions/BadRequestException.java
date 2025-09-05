package com.sigmap.common.docs.exeptions;

import lombok.Data;

@Data
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
