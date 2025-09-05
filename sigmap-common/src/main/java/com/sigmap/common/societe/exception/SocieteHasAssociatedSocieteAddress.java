package com.sigmap.common.societe.exception;

import lombok.Data;

@Data
public class SocieteHasAssociatedSocieteAddress extends RuntimeException {
    private final Integer id;
    public SocieteHasAssociatedSocieteAddress(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
