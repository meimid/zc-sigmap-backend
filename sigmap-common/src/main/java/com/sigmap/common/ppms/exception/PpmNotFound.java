package com.sigmap.common.ppms.exception;


import lombok.Getter;

import java.util.List;
@Getter
public class PpmNotFound extends Exception{
    private final List<Integer> id;
    private final List<Integer> version;

    public PpmNotFound(String message,List<Integer> id, List<Integer> version) {
        super(message);
        this.id = id;
        this.version = version;
    }
}
