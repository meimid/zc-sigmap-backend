package com.sigmap.common.activite.exception;

import lombok.Getter;

@Getter
public class RequiredLibelle extends RuntimeException{

    public RequiredLibelle(String message) {
        super(message);
    }
}
