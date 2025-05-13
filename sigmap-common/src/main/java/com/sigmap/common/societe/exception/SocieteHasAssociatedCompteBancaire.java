package com.sigmap.common.societe.exception;

import lombok.Data;

@Data
public class SocieteHasAssociatedCompteBancaire extends RuntimeException {
    private final Integer id;
    public SocieteHasAssociatedCompteBancaire(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
