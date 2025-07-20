package com.sigmap.common.realisations.response;

import com.sigmap.common.realisations.Realisation;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class RealisationPpmWebsite {
    private Integer id;
    private String expenseObject;
    private String budgetAllocation;
    private String contractType;
    private String selectionMode;
    private String launchDate;
    private String awardDate;

    /**
     * Constructs and maps a Realisation entity to this DTO.
     * @param realisation The source Realisation entity.
     */
    public RealisationPpmWebsite(Realisation realisation) {
        // --- Direct & Simple Mappings ---
        this.id = realisation.getId();
        this.expenseObject = realisation.getRealisationEnvisagees();

        // --- Mappings from related RefParam entities (assuming getNom() exists) ---
        if (realisation.getSourceFinancement() != null) {
            this.budgetAllocation = realisation.getSourceFinancement().getCode();
        }

        if (realisation.getTypeMarcher() != null) {
            this.contractType = realisation.getTypeMarcher().getCode();
        }

        if (realisation.getModeSelection() != null) {
            this.selectionMode = realisation.getModeSelection().getCode();
        }

        // --- Date Mappings (formatting LocalDate to String "dd/MM/yyyy") ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (realisation.getDatePrevLps() != null) {
            this.launchDate = realisation.getDatePrevLps().format(formatter);
        }

        if (realisation.getDatePrevAc() != null) {
            this.awardDate = realisation.getDatePrevAc().format(formatter);
        }
    }
}
