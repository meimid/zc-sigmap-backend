package com.sigmap.common.agpm.exceptions;

import lombok.Data;
import java.util.List;

@Data
public class AgpmInvalidStateException extends Exception {
    private final List<Integer> agpmIds;
    private final List<Integer> agpmVersions;

    public AgpmInvalidStateException(String message, List<Integer> agpmIds, List<Integer> agpmVersions) {
        super(message);
        this.agpmIds = agpmIds;
        this.agpmVersions = agpmVersions;
    }
}