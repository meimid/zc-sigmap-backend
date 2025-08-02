package com.sigmap.common.financement.exceptions;

import lombok.Data;

/**
 * Exception thrown when trying to create a Financement entity that already exists.
 */
@Data
public class FinancementAlreadyExistsException extends RuntimeException {

    private final Object id;

    public FinancementAlreadyExistsException(String message, Object id) {
        super(message);
        this.id = id;
    }

    public FinancementAlreadyExistsException(String message) {
        super(message);
        this.id = null;
    }
}
