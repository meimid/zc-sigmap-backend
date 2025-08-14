package com.sigmap.common.ac_seuil.response;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class AcSeuilId {
    private AcResponseDto ac;
    private RefParamResponseDto typeMarcher;
    private ModeDePassationSelection modePassation;
}
