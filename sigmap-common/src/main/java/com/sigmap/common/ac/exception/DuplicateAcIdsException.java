package com.sigmap.common.ac.exception;


import lombok.Getter;

import java.util.List;
@Getter
public class DuplicateAcIdsException extends RuntimeException{
    private final List<Integer> id;
    public DuplicateAcIdsException(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
