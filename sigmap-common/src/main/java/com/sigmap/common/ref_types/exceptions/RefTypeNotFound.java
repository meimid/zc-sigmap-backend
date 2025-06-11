package com.sigmap.common.ref_types.exceptions;


import com.sigmap.common.config.RefTypes;
import com.sigmap.common.ref_types.RefType;
import lombok.Getter;

@Getter
public class RefTypeNotFound extends RuntimeException{
    private final RefTypes code;
    public RefTypeNotFound( String message , RefTypes code) {
        super(message);
        this.code = code;
    }
}
