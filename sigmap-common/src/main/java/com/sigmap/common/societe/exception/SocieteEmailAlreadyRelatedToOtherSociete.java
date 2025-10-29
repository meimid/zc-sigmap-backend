package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class SocieteEmailAlreadyRelatedToOtherSociete extends RuntimeException {

    private final String email;

    public SocieteEmailAlreadyRelatedToOtherSociete(String message,String email) {
        super(message);
        this.email = email;
    }
}