package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

import java.util.List;
@Getter
public class RefParamAlredyExist extends RuntimeException {
    private final String code;
    public RefParamAlredyExist(String message, String code) {
        super(message);
        this.code = code;
    }
}
