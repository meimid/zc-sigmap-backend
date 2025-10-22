package com.sigmap.common.dac_dyna_form.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when no DAC–Form relation is found or already deleted.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DacDynaFormNotFoundException extends RuntimeException {
    public DacDynaFormNotFoundException(String message) {
        super(message);
    }
}
