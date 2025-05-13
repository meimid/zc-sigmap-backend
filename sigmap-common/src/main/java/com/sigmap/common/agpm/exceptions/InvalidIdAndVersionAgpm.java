package com.sigmap.common.agpm.exceptions;


import lombok.Getter;

import java.util.List;

@Getter
public class InvalidIdAndVersionAgpm extends Exception {

    private final List<Integer> id;
    private final List<Integer> version;


    public InvalidIdAndVersionAgpm(String message, List<Integer> id, List<Integer> version) {
        super(message);
        this.id = id;
        this.version = version;
    }

}
