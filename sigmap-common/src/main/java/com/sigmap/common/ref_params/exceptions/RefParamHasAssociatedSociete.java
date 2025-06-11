package com.sigmap.common.ref_params.exceptions;

import lombok.Data;

@Data
public class RefParamHasAssociatedSociete extends RuntimeException {
    private final Integer id;
    public RefParamHasAssociatedSociete(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
