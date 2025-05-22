package com.sigmap.common.docs.exeptions;

import lombok.Data;

@Data

public class DocsNotFoundException  extends RuntimeException {
    public DocsNotFoundException(String message) {
        super(message);
    }
}
