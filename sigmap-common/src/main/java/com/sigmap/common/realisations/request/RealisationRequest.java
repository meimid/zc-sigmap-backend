package com.sigmap.common.realisations.request;

import com.sigmap.common.lots.LotsId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class RealisationRequest {

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

    // IDs for many-to-many relationships
//    private List<PpmId> ppmIds;
//    private List<PaaId> paaIds;
    private Set<LotsId> lotIds;
    private List<Integer> docIds;

}
