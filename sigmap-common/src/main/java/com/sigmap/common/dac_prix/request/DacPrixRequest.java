package com.sigmap.common.dac_prix.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DacPrixRequest {
    private Integer dacId;
    private Integer dacVer;
    private Integer monnaieId;
    private BigDecimal prix;

}
