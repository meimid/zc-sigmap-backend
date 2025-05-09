package com.sigmap.common.lots.exception;


import lombok.Getter;

import java.util.List;

@Getter
public class LotNotFound extends Exception {
    private final List<Integer> id;
    private final List<Integer> version;

    public LotNotFound(String message, List<Integer> id, List<Integer> version) {
        super(message);
        this.id = id;
        this.version = version;
    }
}
