package com.sigmap.common.lots.response;

import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class LotResponse {
    private Integer id;
    private Integer version;
    private String description;
    private Set<Realisation> realisations;
    private BaseEntityResponse metadata;



}