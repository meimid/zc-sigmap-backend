package com.sigmap.common.agpm.exceptions;


import lombok.Getter;

@Getter
public class MaxUploadSizeFile extends Exception {
    private final long maxFileUploadSize;

    public MaxUploadSizeFile(String message, long maxFileUploadSize) {
        super(message);
        this.maxFileUploadSize = maxFileUploadSize;
    }
}
