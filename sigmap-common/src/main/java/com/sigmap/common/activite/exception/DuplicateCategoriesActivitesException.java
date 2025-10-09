package com.sigmap.common.activite.exception;

import lombok.Getter;

import java.util.List;
@Getter
public class DuplicateCategoriesActivitesException extends RuntimeException{
    private final List<Integer> id;
    public DuplicateCategoriesActivitesException(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
