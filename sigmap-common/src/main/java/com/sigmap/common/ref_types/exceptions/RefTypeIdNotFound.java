package com.sigmap.common.ref_types.exceptions;

import com.sigmap.common.config.RefTypes;
import lombok.Getter;

@Getter
public class RefTypeIdNotFound extends RuntimeException{
    private final Integer id;

    public RefTypeIdNotFound( String message , Integer id) {
        super(message);
        this.id = id;
    }
}



