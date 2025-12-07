package com.sigmap.common.ppms.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Response.DocsResponse;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.realisations.Realisation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.print.Doc;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PpmWebsiteResponse implements Serializable {
    private Integer id;
    private Integer version;
    private String code;
    private String title;
    private String publicationDate;
    private Integer year;
    private Integer nombreActivite;
    private DocsResponseDTO doc;
    private String logo;
    private Integer acId;


    public PpmWebsiteResponse(Ppm ppm) {
        // --- Direct Mappings ---
        this.id = ppm.getId();
        this.version = ppm.getVersion();

        // --- Logic from "title" mapping ---
        if (ppm.getAc() != null) {
            this.acId = ppm.getAc().getId();
            this.title = ppm.getAc().getNom();
                if (ppm.getAc().getLogo() != null) {
                    this.logo = ppm.getAc().getLogo();
                }
        }
        if(ppm.getAnnee() != null) {
            this.year = Integer.valueOf(ppm.getAnnee());
        }

        Calendar updatedAt = ppm.getUpdatedAt();
        if (updatedAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            this.publicationDate = sdf.format(updatedAt.getTime());
        }

        // --- Logic from "generateCode" ---
        /*StringBuilder codeBuilder = new StringBuilder();
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
        }*/
        if(ppm.getNom() != null) {
            this.code = ppm.getRef();
        }

        List<Realisation> sourceRealisations = ppm.getRealisations();
        if (sourceRealisations == null) {
            this.nombreActivite = 0;
        } else {
            this.nombreActivite = sourceRealisations.size();
        }
    }
}
