package com.sigmap.common.dac.exeption;

public class LoadDocumentExeption extends RuntimeException {

    public LoadDocumentExeption() {
        super("Error when load document");
    }

    public LoadDocumentExeption(String message) {
        super(message);
    }

}
