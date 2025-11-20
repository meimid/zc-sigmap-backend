package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class CategorieSoumissionnaireIsRequiredException extends RuntimeException {
    private final Integer id;

    public CategorieSoumissionnaireIsRequiredException(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
