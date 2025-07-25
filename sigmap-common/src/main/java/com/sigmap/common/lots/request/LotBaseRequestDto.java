package com.sigmap.common.lots.request;

import com.sigmap.common.entity.CompositeId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class LotBaseRequestDto {
    private String description;
    private BigDecimal budget;
    private Set<CompositeId> realisationIds;
} 