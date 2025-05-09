package com.sigmap.common.agpm.exceptions;

import lombok.Getter;

@Getter
public class AgpmSoftDelete extends Exception {
    private final Integer number;
    private final Integer id;
    private final Integer version;

    public AgpmSoftDelete(String message, Integer number, Integer id, Integer version) {
        super(message);
        this.number = number;
        this.id = id;
        this.version = version;
    }

}
