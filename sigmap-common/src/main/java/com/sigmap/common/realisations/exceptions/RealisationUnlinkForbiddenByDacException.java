package com.sigmap.common.realisations.exceptions;

import lombok.Getter;

@Getter
public class RealisationUnlinkForbiddenByDacException extends RuntimeException {
    private final Integer id;
    private final Integer version;

    public RealisationUnlinkForbiddenByDacException(Integer id, Integer version) {
        super("Cannot unlink Realisation " + id + " v" + version + " because it is used in a DAC (status != REJETE).");
        this.id = id;
        this.version = version;
    }
}
