package com.sigmap.common.lots.request;

import com.sigmap.common.entity.CompositeId;
import lombok.Data;

import java.util.Set;
@Data
public class LotRequest {
    private Integer lotsId;
    private Integer lotsVer;
    private String description;
    private Set<CompositeId> realisationIds;
}
