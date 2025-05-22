package com.sigmap.common.categorie.exeption;

import lombok.Getter;

@Getter
public class CategorieUpdateExeption extends Exception {
    private final Integer id;
    public CategorieUpdateExeption(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
