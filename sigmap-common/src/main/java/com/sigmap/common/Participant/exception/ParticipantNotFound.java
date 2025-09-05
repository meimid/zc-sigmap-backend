package com.sigmap.common.Participant.exception;

import lombok.Getter;

@Getter
public class ParticipantNotFound  extends RuntimeException {
    Integer id;


    public ParticipantNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
