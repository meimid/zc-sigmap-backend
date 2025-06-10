package com.sigmap.common.dac.exeption;

public class InputFileDocument extends RuntimeException {

    public InputFileDocument() {
        super("Error when input file document");
    }
    public InputFileDocument(String message) {
        super(message);
    }
}
