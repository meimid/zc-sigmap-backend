package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcAlreadyHaveCom extends RuntimeException{
    private final Integer id;

    public AcAlreadyHaveCom(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
