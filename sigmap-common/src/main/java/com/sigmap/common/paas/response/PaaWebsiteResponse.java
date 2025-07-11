package com.sigmap.common.paas.response;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.realisations.Realisation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaaWebsiteResponse {
    private Integer id;
    private Integer version;
    private String code;
    private String title;
    private String publicationDate;
    private Integer year;
    private Integer nombreActivite;
    private List<String> docs;

    public PaaWebsiteResponse(Paa paa) {
        // --- Direct Mappings ---
        this.id = paa.getId();
        this.version = paa.getVersion();

        // --- Logic from "title" mapping ---
        if (paa.getAc() != null) {
            this.title = paa.getAc().getNom();
        }

        // --- Logic from "formatDateToString" and "getYearFromCalendar" ---
        Calendar updatedAt = paa.getUpdatedAt(); // Assumes getUpdatedAt() is inherited
        if (updatedAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            this.publicationDate = sdf.format(updatedAt.getTime());
            this.year = updatedAt.get(Calendar.YEAR);
        }

        // --- Logic from "generateCode" ---
        StringBuilder codeBuilder = new StringBuilder();
        if (paa.getNom() != null) {
            codeBuilder.append(paa.getNom());
        }
        if (paa.getAc() != null && paa.getAc().getNom() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(paa.getAc().getNom());
        }
        if (this.year != null) { // Use the year we already calculated
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(this.year);
        }
        this.code = codeBuilder.toString();

        // --- Logic from "mapDocsToAlfrescoIds" ---
        List<Docs> sourceDocs = paa.getDocs();
        if (sourceDocs == null || sourceDocs.isEmpty()) {
            this.docs = Collections.emptyList();
        } else {
            this.docs = sourceDocs.stream()
                    .map(Docs::getAlfresco_id) // Assumes getAlfresco_id() method
                    .collect(Collectors.toList());
        }
        List<Realisation> sourceRealisations = paa.getRealisations();
        if (sourceRealisations == null) {
            this.nombreActivite = 0;
        } else {
            this.nombreActivite = sourceRealisations.size();
        }
    }

}
