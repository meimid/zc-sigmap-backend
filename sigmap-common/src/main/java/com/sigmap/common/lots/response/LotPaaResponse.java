package com.sigmap.common.lots.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * A simplified lot response DTO specifically for PAA responses.
 * Contains only the essential fields needed when viewing lot data within a PAA.
 */
@Data
public class LotPaaResponse {
    private Integer id;
    private Integer version;
    private String description;
    private BigDecimal budget;
} 