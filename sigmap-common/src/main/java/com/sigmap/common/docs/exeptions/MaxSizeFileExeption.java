package com.sigmap.common.docs.exeptions;

import org.springframework.web.multipart.MaxUploadSizeExceededException;

public class MaxSizeFileExeption extends MaxUploadSizeExceededException {

    private static final long maxUploadSize = 30 * 1024 * 1024; // 30 MB in bytes

    public MaxSizeFileExeption() {
        super(maxUploadSize);
    }

}