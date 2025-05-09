package com.sigmap.common.com.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class ComNotFound extends Exception{

    private final List<Integer> id ;
    public ComNotFound(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
