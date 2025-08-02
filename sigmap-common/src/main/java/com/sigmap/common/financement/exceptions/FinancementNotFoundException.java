package com.sigmap.common.financement.exceptions;

import lombok.Data;

/**
 * Exception thrown when a Financement entity is not found.
 */
@Data
public class FinancementNotFoundException extends RuntimeException {

    private final Object id;

    public FinancementNotFoundException(String message, Object id) {
        super(message);
        this.id = id;
    }

    public FinancementNotFoundException(String message) {
        super(message);
        this.id = null;
    }
}
