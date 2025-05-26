package com.sigmap.common.paas.exceptions;

import lombok.Getter;

@Getter
public class PaaSoftDelete extends Exception {
    private final Integer number;
    private final Integer id;
    private final Integer version;

    public PaaSoftDelete(String message, Integer number, Integer id, Integer version) {
        super(message);
        this.number = number;
        this.id = id;
        this.version = version;
    }
}