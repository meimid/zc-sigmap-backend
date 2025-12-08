package com.sigmap.common.seuil_general.exception;

import lombok.Getter;

@Getter
public class SeuilWithTypeMarcheExiste extends RuntimeException {
    private final Integer id;

    public SeuilWithTypeMarcheExiste(Integer id) {
        this.id = id;
    }
}
