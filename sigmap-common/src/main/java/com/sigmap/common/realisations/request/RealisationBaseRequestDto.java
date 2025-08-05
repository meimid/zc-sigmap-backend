package com.sigmap.common.realisations.request;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.financement.response.FinancementResponse;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.ref_params.RefParam;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class RealisationBaseRequestDto {
    private Integer id;
    private Integer version;
    private String realisationEnvisagees;
    private RefParam service;
    private RefParam direction;
    private RefParam sourceFinancement;
    private List<FinancementRequest> financements;

    private RefParam imputationBudgetaire;
//    private RefParam monais;
    private Monnaie monnaie;
    private RefParam typeMarcher;
    private ModeDePassationSelection modePassation;
//    private RefParam modeSelection;
    private LocalDate datePrevLps;
    private LocalDate datePrevAc;
    private LocalDate datePrevDp;
    private LocalDate datePrevAp;
    private BigDecimal budgetEstime;
    private Boolean isPublished;
    private Boolean publishedAt;
    private Integer sort;
    private RealisationStatus statut;
    private List<Integer> docIds;

    // Remove JsonIgnore to allow lots to be deserialized directly
    private List<LotResponse> lots;

    // Default getter for lots - ensures the collection is initialized
    public List<LotResponse> getLots() {
        if (lots == null) {
            lots = new ArrayList<>();
        }
        return lots;
    }
    private List<MultipartFile> realisationFiles;
}