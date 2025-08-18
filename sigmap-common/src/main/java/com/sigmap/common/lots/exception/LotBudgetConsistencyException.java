package com.sigmap.common.lots.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for inconsistent lot budgets within a realisation.
 */

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LotBudgetConsistencyException extends RuntimeException {
    public LotBudgetConsistencyException(String message) {
        super(message);
    }
}