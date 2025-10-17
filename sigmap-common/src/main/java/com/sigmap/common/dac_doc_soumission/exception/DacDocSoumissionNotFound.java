package com.sigmap.common.dac_doc_soumission.exception;

import lombok.Getter;

@Getter
public class DacDocSoumissionNotFound extends RuntimeException {

    public DacDocSoumissionNotFound(String message) {
        super(message);
    }
}
