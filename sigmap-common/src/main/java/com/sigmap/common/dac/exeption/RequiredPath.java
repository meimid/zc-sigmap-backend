package com.sigmap.common.dac.exeption;

public class RequiredPath extends RuntimeException {

    public RequiredPath() {
        super("Required basePath and customDirPath");
    }
    public RequiredPath(String message) {
        super(message);
    }
}
