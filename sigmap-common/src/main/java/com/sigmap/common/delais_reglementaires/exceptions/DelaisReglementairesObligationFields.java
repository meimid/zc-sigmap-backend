package com.sigmap.common.delais_reglementaires.exceptions;

import lombok.Data;

@Data
public class DelaisReglementairesObligationFields extends RuntimeException {

    public DelaisReglementairesObligationFields(String message) {
        super(message);
    }
}
