package com.sigmap.common.compte_bancaire.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class CompteBancaireNotFound extends Exception{
    private final List<Integer> id;
    public CompteBancaireNotFound(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
