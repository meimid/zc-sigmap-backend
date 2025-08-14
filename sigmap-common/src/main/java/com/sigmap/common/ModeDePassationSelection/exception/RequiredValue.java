package com.sigmap.common.ModeDePassationSelection.exception;

import lombok.Getter;

@Getter
public class RequiredValue extends RuntimeException{

    public RequiredValue(String message) {
        super(message);
    }
}
