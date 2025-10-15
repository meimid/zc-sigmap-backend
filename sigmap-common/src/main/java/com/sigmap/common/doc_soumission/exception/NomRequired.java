package com.sigmap.common.doc_soumission.exception;

import lombok.Getter;

@Getter
public class NomRequired extends RuntimeException {

    public NomRequired(String message) {
        super(message);
    }
}
