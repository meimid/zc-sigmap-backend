package com.sigmap.common.paas.request;

import com.sigmap.common.entity.CompositeId;
import com.sun.istack.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaaRequest {

    private Integer id;

    private Integer version;

    private String ref;

    private String nom;

    private String annee;

    private Integer acId;

    private BigDecimal budgetEstime;

    private Integer statusId;

    private List<CompositeId> realisationIds;
}