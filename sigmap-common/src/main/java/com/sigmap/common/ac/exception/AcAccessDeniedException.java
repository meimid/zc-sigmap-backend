package com.sigmap.common.ac.exception;

import lombok.Getter;


@Getter
public class AcAccessDeniedException extends RuntimeException{
    private final Integer id;

    public AcAccessDeniedException(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
