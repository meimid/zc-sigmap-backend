package com.sigmap.common.docs.exeptions;

import lombok.Getter;

@Getter
public class DocDeleted extends RuntimeException {
    private final Integer id;

    public DocDeleted(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
