package com.sigmap.common.ModeDePassationSelection.exception;

import lombok.Getter;

@Getter
public class ModeDePassationSelectionNotFound extends Exception{
    private final Integer id;

    public ModeDePassationSelectionNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
