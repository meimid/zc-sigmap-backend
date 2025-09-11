package com.sigmap.common.realisations.response;

import com.sigmap.common.realisations.Realisation;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Data
public class RealisationPaaWebSite {
    private Integer id;
    private String expenseObject;
    private String budgetAllocation;
    private String contractType;
    private String launchDate;
    private String awardDate;
    private String modePassation;

    /**
     * Constructs and maps a Realisation entity to this DTO.
     * @param realisation The source Realisation entity.
     */
    public RealisationPaaWebSite(Realisation realisation) {
        // --- Direct & Simple Mappings ---
        this.id = realisation.getId();
        this.expenseObject = realisation.getRealisationEnvisagees();

//        // --- Mappings from related RefParam entities (assuming getNom() exists) ---
        if (realisation.getFinancements() != null) {
            this.budgetAllocation = realisation.getFinancements().stream().map(el->el.getSourceFinancement().getDescription()).collect(Collectors.joining(", "));
       }
        if(realisation.getModePassation() != null) {
            this.modePassation=realisation.getModePassation().getLibelle();
        }

        if (realisation.getTypeMarcher() != null) {
            this.contractType = realisation.getTypeMarcher().getCode();
        }

//        if (realisation.getModeSelection() != null) {
//            this.selectionMode = realisation.getModeSelection().getCode();
//        }


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
