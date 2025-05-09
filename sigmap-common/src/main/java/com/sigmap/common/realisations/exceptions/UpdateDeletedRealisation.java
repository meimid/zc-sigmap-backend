package com.sigmap.common.realisations.exceptions;

import lombok.Getter;

@Getter
public class UpdateDeletedRealisation extends Exception {
    private final Integer id;
    private final Integer version;

    public UpdateDeletedRealisation(String message, Integer id, Integer version ) {
        super(message);
        this.id = id;
        this.version = version;
    }
}
