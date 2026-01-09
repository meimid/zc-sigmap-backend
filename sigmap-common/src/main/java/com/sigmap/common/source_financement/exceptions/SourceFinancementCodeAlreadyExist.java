package com.sigmap.common.source_financement.exceptions;

import lombok.Getter;

@Getter
public class SourceFinancementCodeAlreadyExist extends RuntimeException {
    private final String code;

    public SourceFinancementCodeAlreadyExist(String message, String code) {
        super(message);
        this.code = code;
    }
}
