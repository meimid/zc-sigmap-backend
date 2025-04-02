package com.sigmap.common.societe_contact.exception;

import lombok.Getter;

@Getter
public class SocieteContactNotFound extends Exception {
    private final Integer id;
    public SocieteContactNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public SocieteContactNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}
