package com.sigmap.common.realisations.response;

import com.sigmap.common.realisations.Realisation;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class RealisationPpmWebsite {
    // Fields updated to match the PpmProject interface
    private Integer id;
    private String envisionedDeliverables;
    private String fundingSource;
    private String contractType;
    private String launchDate;
    private String awardDate;
    private String startDate;
    private String completionDate;

    /**
     * Constructs and maps a Realisation entity to this DTO.
     * @param realisation The source Realisation entity.
     */
    public RealisationPpmWebsite(Realisation realisation) {
        this.id = realisation.getId();
        this.envisionedDeliverables = realisation.getRealisationEnvisagees();

//        if (realisation.getSourceFinancement() != null) {
//            this.fundingSource = realisation.getSourceFinancement().getCode();
//        }
        if (realisation.getTypeMarcher() != null) {
            this.contractType = realisation.getTypeMarcher().getCode();
        }

        // --- Date and Time Mappings ---
        // Since the source is LocalDate, it has no time. A default time "00:00" will be used.
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Launch Date (from datePrevLps)
        if (realisation.getDatePrevLps() != null) {
            this.launchDate = realisation.getDatePrevLps().format(dateFormatter);
        }

        // Award Date (from datePrevAc)
        if (realisation.getDatePrevAc() != null) {
            this.awardDate = realisation.getDatePrevAc().format(dateFormatter);
        }

        // Start Date (from datePrevDp, assumed to be 'date prévisionnelle de démarrage')
        if (realisation.getDatePrevDp() != null) {
            this.startDate = realisation.getDatePrevDp().format(dateFormatter);
        }

        // Completion Date (from datePrevAp, assumed to be 'date prévisionnelle d'achèvement')
        if (realisation.getDatePrevAp() != null) {
            this.completionDate = realisation.getDatePrevAp().format(dateFormatter);
        }
    }
}