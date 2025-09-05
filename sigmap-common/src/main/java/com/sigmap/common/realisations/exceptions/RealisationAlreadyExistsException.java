package com.sigmap.common.realisations.exceptions;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class RealisationAlreadyExistsException extends Exception{

    private final List<Integer> id;
    private final List<Integer> version;

    public RealisationAlreadyExistsException(String message,List<Integer> id, List<Integer> version) {
        super(message);
        this.id = id;
        this.version = version;
    }


}
