package com.sigmap.common.direction.exception;

import lombok.Getter;

@Getter
public class LibelleRequired extends RuntimeException {

    public LibelleRequired(String message) {
        super(message);

    }
}
