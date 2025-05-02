package com.sigmap.common.com.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class DuplicateComIdsException extends RuntimeException{
    private final List<Integer> id;
    public DuplicateComIdsException(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
