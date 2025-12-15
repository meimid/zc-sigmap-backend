package com.sigmap.common.appel_offre.request;

import com.sigmap.common.entity.StatusAppelOffre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAppelOffreRequest {
    private Integer ppmId;
    private Integer ppmVer;
    private Integer realisationId;
    private Integer realisationVer;
    private String reference;
    private String nom;
    private String description;
    private String etape;
    private Boolean lotSepares;
    private StatusAppelOffre statusAppelOffre;
}
