package com.sigmap.common.compte_bancaire.exception;

import lombok.Getter;

@Getter
public class CompteBancaireNotFound extends Exception{
    private final Integer id;
    public CompteBancaireNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public CompteBancaireNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}
