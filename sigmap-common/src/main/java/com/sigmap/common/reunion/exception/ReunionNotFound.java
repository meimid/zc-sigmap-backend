package com.sigmap.common.reunion.exception;

import lombok.Getter;

@Getter
public class ReunionNotFound extends RuntimeException {
    Integer id;


    public ReunionNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
