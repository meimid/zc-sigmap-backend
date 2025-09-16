package com.sigmap.common.reunion.exception;

import lombok.Getter;

@Getter
public class DuplicationData extends RuntimeException {


    public DuplicationData(String message) {
        super(message);
    }
}

