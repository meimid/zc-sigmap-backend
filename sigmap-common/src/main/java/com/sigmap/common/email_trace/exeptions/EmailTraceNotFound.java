package com.sigmap.common.email_trace.exeptions;

import lombok.Data;

@Data
public class EmailTraceNotFound extends Exception {
    private Integer id;

    public EmailTraceNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
