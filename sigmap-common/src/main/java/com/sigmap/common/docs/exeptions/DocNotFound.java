package com.sigmap.common.docs.exeptions;

import lombok.Getter;

import java.util.List;


@Getter
public class DocNotFound extends Exception {
    private final List<Integer> id;

    public DocNotFound(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
