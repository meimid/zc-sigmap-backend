package com.sigmap.common.doc_soumission.exception;

import lombok.Getter;

@Getter
public class InvalidTypeDossierException extends RuntimeException {
    private final String invalidValue;

    public InvalidTypeDossierException(String message, String invalidValue) {
        super(message);
        this.invalidValue = invalidValue;
    }
}
