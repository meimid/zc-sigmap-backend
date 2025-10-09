package com.sigmap.common.activite.exception;

import lombok.Getter;

@Getter
public class CategoriesActiviteRequired extends RuntimeException {

    public CategoriesActiviteRequired(String message) {
        super(message);
    }
}
