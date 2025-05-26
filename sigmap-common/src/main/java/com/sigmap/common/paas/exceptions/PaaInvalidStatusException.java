package com.sigmap.common.paas.exceptions;

import lombok.Data;
import java.util.List;

@Data
public class PaaInvalidStatusException extends Exception {
    private final List<Integer> paaIds;
    private final List<Integer> paaVersions;
    private final Integer currentStatusId;

    public PaaInvalidStatusException(String message, List<Integer> paaIds, List<Integer> paaVersions, Integer currentStatusId) {
        super(message);
        this.paaIds = paaIds;
        this.paaVersions = paaVersions;
        this.currentStatusId = currentStatusId;
    }
}