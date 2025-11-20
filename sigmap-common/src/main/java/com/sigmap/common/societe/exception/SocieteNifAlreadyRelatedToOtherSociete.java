package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class SocieteNifAlreadyRelatedToOtherSociete extends RuntimeException {

    private final String nif;

    public SocieteNifAlreadyRelatedToOtherSociete(String message,String nif) {
        super(message);
        this.nif = nif;
    }

}