package com.sigmap.common.ref_params.exceptions;

import lombok.Data;

@Data
public class RefParamHasAssociatedCompteBancaire extends RuntimeException {
    private final Integer id;
    public RefParamHasAssociatedCompteBancaire(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
