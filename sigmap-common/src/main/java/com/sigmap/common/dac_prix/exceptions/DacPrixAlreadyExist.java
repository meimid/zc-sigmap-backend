package com.sigmap.common.dac_prix.exceptions;

import lombok.Data;

@Data
public class DacPrixAlreadyExist extends RuntimeException {
    private Integer dacId;
    private Integer dacVer;
    private Integer monnaieId;
    public DacPrixAlreadyExist(Integer dacId, Integer dacVer, Integer monnaieId) {
        this.dacId = dacId;
        this.dacVer = dacVer;
        this.monnaieId = monnaieId;

    }
}
