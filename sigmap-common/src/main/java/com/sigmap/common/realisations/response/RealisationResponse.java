package com.sigmap.common.realisations.response;

import com.sigmap.common.paas.Paa;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RealisationResponse {
    private Integer id;
    private Integer version;
    private String realisationEnvisagees;
    private Integer serviceId;
    private Integer directionId;
    private Integer sourceFinancementId;
    private Integer monaisId;
    private Integer typeMarcherId;
    private Integer modeSelectionId;
    private LocalDate datePrevLps;
    private LocalDate datePrevAc;
    private LocalDate datePrevDp;
    private LocalDate datePrevAp;
    private BigDecimal budgetEstime;
    private Boolean isPublished;
    private Boolean publishedAt;
    private Integer sort;
    private List<Paa> paas;
    private BaseEntityResponse metadata;
}
