package com.sigmap.common.contact.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ContactNotFound extends RuntimeException {
    private final List<Integer> id;

    public ContactNotFound(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
