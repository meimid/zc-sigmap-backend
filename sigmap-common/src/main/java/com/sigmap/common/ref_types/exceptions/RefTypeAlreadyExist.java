package com.sigmap.common.ref_types.exceptions;

import lombok.Getter;

@Getter
public class RefTypeAlreadyExist extends RuntimeException {
    private final Integer id;
    public RefTypeAlreadyExist( String message, Integer id) {
        super(message);
        this.id = id;
    }
}
