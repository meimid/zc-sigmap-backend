package com.sigmap.common.dac.exeption;

import lombok.Getter;

@Getter
public class DacNotFound extends RuntimeException {
        private Integer dacId;
        private Integer dacVersion;
    public DacNotFound(String message,Integer dacId,Integer dacVersion) {
        super(message);
        this.dacId = dacId;
        this.dacVersion = dacVersion;
    }

    public DacNotFound(Integer dacId) {
        this.dacId = dacId;
    }
}
