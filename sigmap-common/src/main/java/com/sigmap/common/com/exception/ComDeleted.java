package com.sigmap.common.com.exception;

import lombok.Getter;

import java.util.List;
@Getter
public class ComDeleted extends RuntimeException{
    private final List<Integer> id;
    public ComDeleted(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
