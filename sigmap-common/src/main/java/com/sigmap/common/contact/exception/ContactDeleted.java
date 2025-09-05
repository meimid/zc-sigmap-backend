package com.sigmap.common.contact.exception;

import lombok.Getter;

import java.util.List;
@Getter
public class ContactDeleted extends RuntimeException {
    private final List<Integer> id;

    public ContactDeleted(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}
