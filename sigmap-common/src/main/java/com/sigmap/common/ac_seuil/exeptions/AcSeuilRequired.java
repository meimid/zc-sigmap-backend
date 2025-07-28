package com.sigmap.common.ac_seuil.exeptions;

import lombok.Data;

@Data
public class AcSeuilRequired extends RuntimeException{
    public AcSeuilRequired(String message) {
        super(message);
    }
}
