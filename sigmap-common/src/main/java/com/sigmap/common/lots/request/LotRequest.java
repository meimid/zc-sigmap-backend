package com.sigmap.common.lots.request;

import com.sigmap.common.entity.CompositeId;
import lombok.Data;

import java.util.Set;
@Data
public class LotRequest {
    private Integer id;
    private Integer version;
    private String description;
    private Set<CompositeId> realisationIds;
}
