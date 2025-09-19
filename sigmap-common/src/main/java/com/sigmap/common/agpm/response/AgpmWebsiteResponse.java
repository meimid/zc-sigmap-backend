package com.sigmap.common.agpm.response;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Response.DocsResponseDTO;
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
    private String logo;
    private DocsResponseDTO doc;
    private Integer acId;
    private Boolean actualise;
    private Integer nbrActualisation;

    public AgpmWebsiteResponse(Agpm agpm) {
        // --- Direct Mappings ---
        this.id = agpm.getId();
        this.version = agpm.getVersion();

        // --- Logic from "title" mapping ---
        if (agpm.getAc() != null) {
            this.acId = agpm.getAc().getId();
            this.title = agpm.getAc().getNom();
            if (agpm.getAc().getLogo() != null) {
                this.logo = agpm.getAc().getLogo();
            }
        }


        Calendar updatedAt = agpm.getUpdatedAt();
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

    }
}
