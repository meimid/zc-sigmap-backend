package com.sigmap.common.com.exception;

import lombok.Getter;

@Getter
public class ComNotFound extends Exception{
    public ComNotFound(String message) {
        super(message);
    }
}
