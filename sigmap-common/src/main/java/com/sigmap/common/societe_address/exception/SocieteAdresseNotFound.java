package com.sigmap.common.societe_address.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class SocieteAdresseNotFound extends RuntimeException {
    private final Integer societeId;
    private final Integer adresseId;
    public SocieteAdresseNotFound(String message , Integer societeId, Integer adresseId) {
        super(message);
        this.societeId = societeId;
        this.adresseId = adresseId;
    }

}
