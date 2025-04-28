
package com.sigmap.common.ac.exception;

import lombok.Getter;

@Getter
public class AcNotFound extends Exception{

    public AcNotFound(String message) {
        super(message);
    }
}

