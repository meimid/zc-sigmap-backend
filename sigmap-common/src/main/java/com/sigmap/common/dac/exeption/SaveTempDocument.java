package com.sigmap.common.dac.exeption;

public class SaveTempDocument extends RuntimeException {

    public SaveTempDocument() {
        super("Error when save temp document");
    }
    public SaveTempDocument(String message) {
        super(message);
    }
}
