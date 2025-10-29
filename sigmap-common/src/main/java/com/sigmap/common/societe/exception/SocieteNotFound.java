package com.sigmap.common.societe.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class SocieteNotFound extends RuntimeException {
    private final List<Integer> id;
    public SocieteNotFound( String message ,List<Integer> id) {
        super(message);
        this.id = id;
    }

}
