package com.sigmap.common.agpm.exceptions;


import lombok.Getter;

@Getter
public class AgpmNotNull extends Exception{

    public AgpmNotNull(String message) {
        super(message);
    }

}
