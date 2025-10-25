package com.sigmap.common.societe_dac.exceptions;

import lombok.Data;

@Data
public class SocieteDacAlreadyExistsException extends RuntimeException {
    Integer dacId;
    Integer dacVersion;
    Integer societeId;

    public SocieteDacAlreadyExistsException(Integer dacId, Integer dacVersion, Integer societeId) {
        this.dacId = dacId;
        this.dacVersion = dacVersion;
        this.societeId = societeId;
    }
}
