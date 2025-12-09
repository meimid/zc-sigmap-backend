package com.sigmap.common.Participant.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyReservedForAotherUser extends RuntimeException {
    String email;


    public EmailAlreadyReservedForAotherUser(String message, String email) {
        super(message);
        this.email = email;
    }
}
