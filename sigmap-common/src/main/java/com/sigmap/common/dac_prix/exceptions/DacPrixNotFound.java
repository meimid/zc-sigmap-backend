package com.sigmap.common.dac_prix.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
public class DacPrixNotFound extends RuntimeException {
    private Integer dacId;
    private Integer dacVer;
    private Integer monnaieId;
    public DacPrixNotFound(Integer dacId, Integer dacVer, Integer monnaieId) {
        this.dacId = dacId;
        this.dacVer = dacVer;
        this.monnaieId = monnaieId;

    }
}
