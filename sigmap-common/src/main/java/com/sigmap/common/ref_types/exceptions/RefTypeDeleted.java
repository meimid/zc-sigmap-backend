package com.sigmap.common.ref_types.exceptions;

import lombok.Getter;

@Getter
public class RefTypeDeleted extends RuntimeException{
    private final Integer id;
    public RefTypeDeleted( String message , Integer id) {
        super(message);
        this.id = id;
    }
}
