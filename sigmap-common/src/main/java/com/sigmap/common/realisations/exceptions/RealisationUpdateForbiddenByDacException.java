package com.sigmap.common.realisations.exceptions;

import lombok.Getter;

@Getter
public class RealisationUpdateForbiddenByDacException extends RuntimeException {
    private final Integer id;
    private final Integer version;

    public RealisationUpdateForbiddenByDacException(Integer id, Integer version) {
        super("Cannot update Realisation " + id + " v" + version + " because it is used in a DAC (status != REJETE).");
        this.id = id;
        this.version = version;
    }
}
