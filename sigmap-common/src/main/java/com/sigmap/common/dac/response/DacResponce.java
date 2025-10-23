package com.sigmap.common.dac.response;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class DacResponce {
    private Integer dacId;
    private Integer dacVer;
    private String reference;
    private Calendar dateLimiteSoumission;
    private Calendar dateLimiteRecptionDemande;
    private boolean payant;
    private PpmResponseDto ppm;
    private RealisationResponseDTO realisation;
    private RefParam typeMarche;
    private ModeDePassationSelection modePassation;
    private ProcessStatus status;
    private String processInstanceId;
    private Integer parentDacId;
    private boolean shortListing;
    private BaseEntityResponse metadata;
}
