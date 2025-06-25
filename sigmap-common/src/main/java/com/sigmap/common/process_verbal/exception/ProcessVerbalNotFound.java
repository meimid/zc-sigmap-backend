package com.sigmap.common.process_verbal.exception;

import lombok.Getter;

@Getter
public class ProcessVerbalNotFound extends RuntimeException {
    Integer ppmId;
    Integer ppmVer;
    String processVerbalId;

    public ProcessVerbalNotFound(String message,Integer ppmId,Integer ppmVer,String processVerbalId) {
        super(message);
        this.ppmId = ppmId;
        this.ppmVer = ppmVer;
        this.processVerbalId = processVerbalId;
    }
}
