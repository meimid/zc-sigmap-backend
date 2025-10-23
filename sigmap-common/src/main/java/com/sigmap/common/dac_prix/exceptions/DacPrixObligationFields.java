package com.sigmap.common.dac_prix.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée lorsque des champs obligatoires sont manquants pour un DacPrix
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DacPrixObligationFields extends RuntimeException {
    public DacPrixObligationFields(String message) {
        super(message);
    }
}
