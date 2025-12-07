package com.sigmap.common.seuil_general.exception;

import lombok.Getter;

@Getter
public class SeuilGeneralNotFound extends RuntimeException {
    private final Integer id;
    public SeuilGeneralNotFound(Integer id) {
        this.id =id;

    }
}
