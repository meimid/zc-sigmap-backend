package com.sigmap.common.activite.exception;

import lombok.Getter;

@Getter
public class HasAssociatedSociete extends RuntimeException{

    public HasAssociatedSociete(String message) {
        super(message);
    }
}

