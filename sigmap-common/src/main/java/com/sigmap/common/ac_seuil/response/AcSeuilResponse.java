package com.sigmap.common.ac_seuil.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AcSeuilResponse {
    private AcSeuilId id;
    private BigDecimal seuil;
}
