package com.sigmap.common.paas.exceptions;

import java.util.List;

public class PaaInvalidStateException extends Exception {
    private final List<Integer> id;
    private final List<Integer> version;

    public PaaInvalidStateException(String message, List<Integer> id, List<Integer> version) {
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