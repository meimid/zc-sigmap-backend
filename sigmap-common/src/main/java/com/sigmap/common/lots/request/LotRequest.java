package com.sigmap.common.lots.request;

import com.sigmap.common.entity.CompositeId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
@Data
public class LotRequest {
    private Integer id;
    private Integer version;
    private String description;
    private BigDecimal budget;
    private Set<CompositeId> realisationIds;
}
