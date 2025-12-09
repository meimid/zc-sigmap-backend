package com.sigmap.common.seuil_general.response;

import com.sigmap.common.ref_params.RefParam;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeuilGeneralResponse {
    private Integer id;
    private RefParam typeMarcher;
    private BigDecimal montant;
}
