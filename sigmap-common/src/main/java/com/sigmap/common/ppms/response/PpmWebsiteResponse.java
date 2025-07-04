package com.sigmap.common.ppms.response;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.ppms.Ppm;
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
public class PpmWebsiteResponse {
    private Integer id;
    private Integer version;
    private String code;
    private String title;
    private String publicationDate;
    private Integer year;
    private List<String> docs;

    public PpmWebsiteResponse(Ppm ppm) {
        // --- Direct Mappings ---
        this.id = ppm.getId();
        this.version = ppm.getVersion();

        // --- Logic from "title" mapping ---
        if (ppm.getAc() != null) {
            this.title = ppm.getAc().getNom();
        }

        // --- Logic from "formatDateToString" and "getYearFromCalendar" ---
        Calendar updatedAt = ppm.getUpdatedAt(); // Assumes getUpdatedAt() is inherited
        if (updatedAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            this.publicationDate = sdf.format(updatedAt.getTime());
            this.year = updatedAt.get(Calendar.YEAR);
        }

        // --- Logic from "generateCode" ---
        StringBuilder codeBuilder = new StringBuilder();
        if (ppm.getNom() != null) {
            codeBuilder.append(ppm.getNom());
        }
        if (ppm.getAc() != null && ppm.getAc().getNom() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(ppm.getAc().getNom());
        }
        if (this.year != null) { // Use the year we already calculated
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(this.year);
        }
        this.code = codeBuilder.toString();

        // --- Logic from "mapDocsToAlfrescoIds" ---
        List<Docs> sourceDocs = ppm.getDocs();
        if (sourceDocs == null || sourceDocs.isEmpty()) {
            this.docs = Collections.emptyList();
        } else {
            this.docs = sourceDocs.stream()
                    .map(Docs::getAlfresco_id) // Assumes getAlfresco_id() method
                    .collect(Collectors.toList());
        }
    }
}
