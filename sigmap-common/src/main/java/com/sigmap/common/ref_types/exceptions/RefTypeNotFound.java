package com.sigmap.common.ref_types.exceptions;


import lombok.Getter;

@Getter
public class RefTypeNotFound extends RuntimeException{
    private final Integer id;
    public RefTypeNotFound( String message , Integer id) {
        super(message);
        this.id = id;
    }
}
