package com.sigmap.common.monnaie.exceptions;

import lombok.Getter;
import java.util.List;

@Getter
public class MonnaieNotFound extends Exception {
    private final List<Integer> id;
    public MonnaieNotFound(String message , List<Integer> id) {
        super(message);
        this.id = id;
    }
    public MonnaieNotFound(String message, Throwable cause, List<Integer> id) {
        super(message, cause);
        this.id = id;
    }

}

