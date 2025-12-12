package com.sigmap.common.appel_offre.exception;

import lombok.Getter;

@Getter
public class AppelOffreNotFound extends RuntimeException {
    private final Integer id;

    public AppelOffreNotFound( Integer id) {
        this.id = id;
    }
}
