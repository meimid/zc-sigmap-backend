package com.sigmap.common.source_financement.exceptions;

import lombok.Getter;
import java.util.List;

@Getter
public class SourceFinancementNotFound extends Exception {
    private final List<Integer> id;

    public SourceFinancementNotFound(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }

    public SourceFinancementNotFound(String message, Throwable cause, List<Integer> id) {
        super(message, cause);
        this.id = id;
    }
}
