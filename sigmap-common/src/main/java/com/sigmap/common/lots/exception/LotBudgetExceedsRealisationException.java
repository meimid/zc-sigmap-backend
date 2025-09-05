package com.sigmap.common.lots.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LotBudgetExceedsRealisationException extends RuntimeException {
    public LotBudgetExceedsRealisationException(String message) {
        super(message);
    }
}
