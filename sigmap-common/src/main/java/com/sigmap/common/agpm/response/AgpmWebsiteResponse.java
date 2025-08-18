package com.sigmap.common.agpm.response;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.docs.Docs;
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
public class AgpmWebsiteResponse {
    private Integer id;
    private Integer version;
    private String code;
    private String title;
    private String publicationDate;
    private Integer year;
    private List<String> docs;

    public AgpmWebsiteResponse(Agpm agpm) {
        // --- Direct Mappings ---
        this.id = agpm.getId();
        this.version = agpm.getVersion();

        // --- Logic from "title" mapping ---
        if (agpm.getAc() != null) {
            this.title = agpm.getAc().getNom();
        }

        // --- Logic from "formatDateToString" and "getYearFromCalendar" ---
        Calendar updatedAt = agpm.getUpdatedAt(); // Assumes getUpdatedAt() is inherited
        if (updatedAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            this.publicationDate = sdf.format(updatedAt.getTime());
            this.year = updatedAt.get(Calendar.YEAR);
        }

        // --- Logic from "generateCode" ---
        StringBuilder codeBuilder = new StringBuilder();
        if (agpm.getNom() != null) {
            codeBuilder.append(agpm.getNom());
        }
        if (agpm.getAc() != null && agpm.getAc().getNom() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(agpm.getAc().getNom());
        }
        if (this.year != null) { // Use the year we already calculated
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(this.year);
        }
        this.code = codeBuilder.toString();

        // --- Logic from "mapDocsToAlfrescoIds" ---
        List<Docs> sourceDocs = agpm.getDocs();
        if (sourceDocs == null || sourceDocs.isEmpty()) {
            this.docs = Collections.emptyList();
        } else {
            this.docs = sourceDocs.stream()
                    .map(Docs::getAlfresco_id) // Assumes getAlfresco_id() method
                    .collect(Collectors.toList());
        }
    }
}
