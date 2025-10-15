package com.sigmap.common.type_agrement.exception;

import lombok.Getter;

@Getter
public class TypeAgrementNotFound extends Exception{
    private final Integer id;

    public TypeAgrementNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
