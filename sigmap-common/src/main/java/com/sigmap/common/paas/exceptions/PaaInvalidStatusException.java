package com.sigmap.common.paas.exceptions;

import com.sigmap.common.entity.ProcessStatus;
import lombok.Data;
import java.util.List;

@Data
public class PaaInvalidStatusException extends Exception {
    private final List<Integer> paaIds;
    private final List<Integer> paaVersions;
    private final ProcessStatus currentStatus;

    public PaaInvalidStatusException(String message, List<Integer> paaIds, List<Integer> paaVersions, ProcessStatus currentStatus) {
        super(message);
        this.paaIds = paaIds;
        this.paaVersions = paaVersions;
        this.currentStatus = currentStatus;
    }
}