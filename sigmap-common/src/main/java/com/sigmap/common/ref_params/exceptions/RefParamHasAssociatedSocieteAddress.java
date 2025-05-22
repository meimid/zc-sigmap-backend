package com.sigmap.common.ref_params.exceptions;

import lombok.Data;

@Data
public class RefParamHasAssociatedSocieteAddress extends RuntimeException {
    private final Integer id;
    public RefParamHasAssociatedSocieteAddress(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
