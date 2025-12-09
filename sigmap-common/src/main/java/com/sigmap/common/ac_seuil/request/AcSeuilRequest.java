package com.sigmap.common.ac_seuil.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AcSeuilRequest {
    private Integer acId;
    private Integer typeMarcherId;
    private BigDecimal seuil;
}
