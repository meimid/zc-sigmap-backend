package com.sigmap.common.dac.exeption;

import lombok.Getter;

@Getter
public class DacWithRealisation extends RuntimeException {
    private final Integer id;
    private final Integer version;

    public DacWithRealisation( Integer id, Integer version) {
        this.id = id;
        this.version = version;
    }
}
