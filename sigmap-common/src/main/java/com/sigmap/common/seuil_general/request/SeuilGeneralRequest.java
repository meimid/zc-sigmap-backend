package com.sigmap.common.seuil_general.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SeuilGeneralRequest {
    private Integer typeMarcherId;
    private BigDecimal montant;
}
