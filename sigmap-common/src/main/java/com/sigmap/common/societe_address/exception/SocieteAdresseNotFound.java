package com.sigmap.common.societe_address.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class SocieteAdresseNotFound extends Exception {
    private final List<Integer> id;
    public SocieteAdresseNotFound(String message , List<Integer> id) {
        super(message);
        this.id = id;
    }

}
