package com.sigmap.common.lots.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LotResponseDTO {
    private Integer id;
    private Integer version;
    private String description;
    private BigDecimal budget;
}
