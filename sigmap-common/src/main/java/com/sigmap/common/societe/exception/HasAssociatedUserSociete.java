package com.sigmap.common.societe.exception;

import lombok.Getter;

@Getter
public class HasAssociatedUserSociete extends RuntimeException {
    private final Integer id;

    public HasAssociatedUserSociete(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
