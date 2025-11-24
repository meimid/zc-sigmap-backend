package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class SocieteNumCnssAlreadyRelatedToOtherSociete extends RuntimeException {

    private final String numCnss;

    public SocieteNumCnssAlreadyRelatedToOtherSociete(String message,String numCnss) {
        super(message);
        this.numCnss = numCnss;
    }


}