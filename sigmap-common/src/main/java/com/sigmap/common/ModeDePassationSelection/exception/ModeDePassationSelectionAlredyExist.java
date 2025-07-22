package com.sigmap.common.ModeDePassationSelection.exception;

import lombok.Getter;

@Getter
public class ModeDePassationSelectionAlredyExist extends RuntimeException{
    private final String libelle;

    public ModeDePassationSelectionAlredyExist(String message, String libelle) {
        super(message);
        this.libelle = libelle;
    }
}
