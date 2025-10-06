package com.sigmap.common.dyna_docs.exception;

import lombok.Getter;

@Getter
public class ActiveAAOAlreadyExistsException extends RuntimeException {
    private final Integer typeMarcheId;
    private final Integer modePassationId;

    public ActiveAAOAlreadyExistsException(Integer typeMarcheId, Integer modePassationId) {
        this.typeMarcheId = typeMarcheId;
        this.modePassationId = modePassationId;
    }

    public ActiveAAOAlreadyExistsException(Integer typeMarcheId) {
        super(String.format("An active AAO DynaDocs already exists for typeMarche: %d", typeMarcheId));
        this.typeMarcheId = typeMarcheId;
        this.modePassationId = null;
    }
} 