
package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcNotFound extends Exception{
    private final Integer id;
    public AcNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public AcNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}

