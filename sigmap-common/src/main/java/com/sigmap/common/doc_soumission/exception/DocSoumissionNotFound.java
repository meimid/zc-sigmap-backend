package com.sigmap.common.doc_soumission.exception;

import lombok.Getter;

@Getter
public class DocSoumissionNotFound  extends RuntimeException{
    private final Integer id;
    public DocSoumissionNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
