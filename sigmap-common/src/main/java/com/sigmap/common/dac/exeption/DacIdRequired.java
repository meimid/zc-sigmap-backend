package com.sigmap.common.dac.exeption;

import lombok.Getter;

@Getter
public class DacIdRequired extends RuntimeException {

    public DacIdRequired(String message) {
        super(message);

    }
}
