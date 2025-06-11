package com.sigmap.common.paas.exceptions;


import lombok.Getter;

import java.util.List;

@Getter
public class PaaNotFound extends Exception {
    private final List<Integer> id;
    private final List<Integer> version;

    public PaaNotFound(String message, List<Integer> id, List<Integer> version) {
        super(message);
        this.id = id;
        this.version = version;
    }

    public List<Integer> getId() {
        return id;
    }

    public List<Integer> getVersion() {
        return version;
    }


}
