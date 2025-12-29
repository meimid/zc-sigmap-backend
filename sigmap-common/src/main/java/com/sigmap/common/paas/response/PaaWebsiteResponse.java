package com.sigmap.common.paas.response;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.docs.Response.DocsResponseWebsiteDTO;
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
    private String code;
    private String title;
    private String publicationDate;
    private Integer year;
    private Integer nombreActivite;
    private String logo;
    private DocsResponseWebsiteDTO doc;
    private Integer acId;

    public PaaWebsiteResponse(Paa paa) {
        // --- Direct Mappings ---
        this.id = paa.getId();

        // --- Logic from "title" mapping ---
        if (paa.getAc() != null) {
            this.acId = paa.getAc().getId();
            this.title = paa.getAc().getNom();
            if(paa.getAc().getLogo() != null) {
                this.logo = paa.getAc().getLogo();
            }
        }
        if(paa.getAnnee() != null) {
            this.year = Integer.valueOf(paa.getAnnee());
        }

        Calendar updatedAt = paa.getPublishedAt();
        if (updatedAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            this.publicationDate = sdf.format(updatedAt.getTime());
        }


        if(paa.getNom() != null) {
            this.code=paa.getNom();
        }
      //  this.code = codeBuilder.toString();

        List<Realisation> sourceRealisations = paa.getRealisations();
        if (sourceRealisations == null) {
            this.nombreActivite = 0;
        } else {
            this.nombreActivite = sourceRealisations.size();
        }
    }

}
