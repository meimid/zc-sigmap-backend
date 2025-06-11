package com.sigmap.common.agpm.exceptions;

import java.util.List;

public class AgpmNotAssociatedWithUserAcException extends RuntimeException {
    public AgpmNotAssociatedWithUserAcException(String message, List<Integer> agpmIds, List<Integer> agpmVersions) {
        super(message);
    }
}
