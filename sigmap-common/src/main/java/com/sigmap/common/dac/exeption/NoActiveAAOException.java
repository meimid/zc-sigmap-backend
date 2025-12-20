package com.sigmap.common.dac.exeption;

import lombok.Getter;

@Getter
public class NoActiveAAOException extends RuntimeException {
    private final Integer typeMarcheId;
    private final Integer modePassationId;

    public NoActiveAAOException(Integer typeMarcheId, Integer modePassationId) {
        this.typeMarcheId = typeMarcheId;
        this.modePassationId = modePassationId;
    }

    public NoActiveAAOException(Integer typeMarcheId) {
        super(String.format("No active AAO DynaDocs found for typeMarche: %d. At least one active AAO DynaDocs is required for DAC creation.", 
              typeMarcheId));
        this.typeMarcheId = typeMarcheId;
        this.modePassationId = null;
    }
} 