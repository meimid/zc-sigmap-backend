package com.sigmap.common.process_verbal_reunion.exception;

import lombok.Getter;

@Getter
public class ProcessVerbalReunionNotFound  extends RuntimeException {
    Integer id;


    public ProcessVerbalReunionNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
