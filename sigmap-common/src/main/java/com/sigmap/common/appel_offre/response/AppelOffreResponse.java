package com.sigmap.common.appel_offre.response;

import com.sigmap.common.config.ModePassationType;
import com.sigmap.common.entity.StatusAppelOffre;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppelOffreResponse {
    private Integer id;
    private PpmResponseDto ppm;
    private RealisationResponseDTO realisation;
    private String reference;
    private String nom;
    private String description;
    private String etape;
    private ModePassationType modePassation;
    private Boolean lotSepares;
    private StatusAppelOffre statusAppelOffre;
    private BaseEntityResponse metadata;
}
