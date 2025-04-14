package com.sigmap.common.ppms.request;


import com.sun.istack.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PpmRequest {

    private Integer id;

    private Integer version;

    private String ref;

    private String nom;

    private String annee;

    private Integer acId;

    private BigDecimal budgetEstime;

    private Integer statusId;
}