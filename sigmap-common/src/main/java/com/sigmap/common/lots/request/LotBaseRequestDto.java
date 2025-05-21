package com.sigmap.common.lots.request;

import com.sigmap.common.entity.CompositeId;
import lombok.Data;

import java.util.Set;

@Data
public class LotBaseRequestDto {
    private String description;
    private Set<CompositeId> realisationIds;
} 