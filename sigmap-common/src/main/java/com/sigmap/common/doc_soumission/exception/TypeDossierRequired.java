package com.sigmap.common.doc_soumission.exception;

import lombok.Getter;

@Getter
public class TypeDossierRequired extends RuntimeException {

    public TypeDossierRequired(String message) {
        super(message);
    }
}
