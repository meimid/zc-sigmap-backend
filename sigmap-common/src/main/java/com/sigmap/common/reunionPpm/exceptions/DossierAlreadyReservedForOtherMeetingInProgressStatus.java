package com.sigmap.common.reunionPpm.exceptions;

import lombok.Getter;

@Getter
public class DossierAlreadyReservedForOtherMeetingInProgressStatus extends RuntimeException {

    String ref;
    public DossierAlreadyReservedForOtherMeetingInProgressStatus(String message, String ref) {
            super(message);
            this.ref = ref;
        }

}
