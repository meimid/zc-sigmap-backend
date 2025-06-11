package com.sigmap.common.agpm.exceptions;

import lombok.Data;

import java.util.List;

@Data
public class AgpmNotFound extends Exception {
    private final List<Integer> id;
    private final List<Integer> version;


    public AgpmNotFound(String message, List<Integer> id, List<Integer> version) {
        super(message);
        this.id = id;
        this.version = version;
    }

}