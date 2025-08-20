package com.sigmap.common.reunion.exception;

import com.sigmap.common.config.ReunionStatus;
import lombok.Getter;

@Getter
public class ReunionStatusException extends RuntimeException {
    ReunionStatus status;

    public ReunionStatusException(String message, ReunionStatus status) {
        super(message);
        this.status = status;
    }
}