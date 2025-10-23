package com.sigmap.common.dac_prix.response;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.monnaie.Monnaie;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DacPrixResponce {
   // private Dac dac;
    private Integer dacId;
    private Integer dacVer;
    private Monnaie monnaie;
    private BigDecimal prix;
}
