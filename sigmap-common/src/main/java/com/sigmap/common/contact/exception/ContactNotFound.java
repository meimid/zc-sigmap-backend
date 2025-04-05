package com.sigmap.common.contact.exception;

import lombok.Getter;

@Getter
public class ContactNotFound extends RuntimeException {
    private final Integer id;

    public ContactNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public ContactNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}
