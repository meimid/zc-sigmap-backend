package com.sigmap.common.ModeDePassationSelection.exception;

import lombok.Getter;

import java.util.List;
@Getter
public class DuplicateTypeMarcherIdsException extends RuntimeException{
    private final List<Integer> id;
    public DuplicateTypeMarcherIdsException(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
