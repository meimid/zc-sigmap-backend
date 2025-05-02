
package com.sigmap.common.ac.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class AcNotFound extends Exception{
    private final List<Integer> id;

    public AcNotFound(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}

