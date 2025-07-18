package com.sigmap.common.ModeDePassationSelection.exception;

import lombok.Getter;

import java.util.List;
@Getter
public class DuplicateCategorieIdsException extends RuntimeException{
    private final List<Integer> id;
    public DuplicateCategorieIdsException(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
