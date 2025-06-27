package com.sigmap.common.ppms.exception;

import lombok.Data;

@Data
public class PpmCreationLimitExceededException extends RuntimeException {
        private final Integer id;
        private final Integer version;
        private final int limit;

        // Constructor with id, version, limit, and message
    public PpmCreationLimitExceededException(String message, Integer id, Integer version, int limit) {
            super(message);
            this.id = id;
            this.version = version;
            this.limit = limit;
        }
    }

