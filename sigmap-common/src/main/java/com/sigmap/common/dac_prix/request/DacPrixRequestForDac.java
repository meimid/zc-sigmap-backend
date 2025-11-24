package com.sigmap.common.dac_prix.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DacPrixRequestForDac {
    private Integer monnaieId;
    private BigDecimal prix;
}
