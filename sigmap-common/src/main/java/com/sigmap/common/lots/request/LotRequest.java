package com.sigmap.common.lots.request;

import lombok.Data;

import java.util.Set;
@Data
public class LotRequest {
    private Integer lotsId;
    private Integer lotsVer;
    private String description;
    private Set<Integer> realisationIds;
}
