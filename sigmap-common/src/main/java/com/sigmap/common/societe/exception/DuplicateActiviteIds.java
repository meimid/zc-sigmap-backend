package com.sigmap.common.societe.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class DuplicateActiviteIds extends RuntimeException{
    private final List<Integer> id;
    public DuplicateActiviteIds(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}

