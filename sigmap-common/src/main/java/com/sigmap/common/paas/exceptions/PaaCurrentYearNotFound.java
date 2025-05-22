package com.sigmap.common.paas.exceptions;

import com.sigmap.common.paas.Paa;
import lombok.Getter;

@Getter
public class PaaCurrentYearNotFound extends RuntimeException {

    private final String currentYear;

    public PaaCurrentYearNotFound(String message, String currentYear) {
        super(message);
        this.currentYear = currentYear;
    }
}
