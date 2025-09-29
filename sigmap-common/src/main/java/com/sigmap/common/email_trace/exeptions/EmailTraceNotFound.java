package com.sigmap.common.email_trace.exeptions;

import lombok.Data;

@Data
public class EmailTraceNotFound extends Exception {
    private Long id;

    public EmailTraceNotFound(String message, Long id) {
        super(message);
        this.id = id;
    }
}
