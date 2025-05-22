package com.sigmap.common.categorie.exeption;

import lombok.Getter;

import java.util.List;

@Getter
public class CategorieNotFound extends Exception {
    private final List<Integer> id;

    public CategorieNotFound(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
