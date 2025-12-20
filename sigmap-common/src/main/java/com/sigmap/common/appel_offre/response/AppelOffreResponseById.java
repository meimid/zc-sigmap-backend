package com.sigmap.common.appel_offre.response;

import com.sigmap.common.entity.StatusAppelOffre;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.response.RealisationResponse;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import com.sigmap.common.realisations.response.RealisationResponseForAO;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppelOffreResponseById {
    private Integer id;
    private PpmResponseDto ppm;
    private RealisationResponseForAO realisation;
    private String reference;
    private String nom;
    private String description;
    private String etape;
    private String modePassation;
    private Boolean lotSepares;
    private StatusAppelOffre statusAppelOffre;
    boolean couldCreateDac;
    private BaseEntityResponse metadata;
}

