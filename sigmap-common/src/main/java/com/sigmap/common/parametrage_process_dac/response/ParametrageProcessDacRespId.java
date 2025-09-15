package com.sigmap.common.parametrage_process_dac.response;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class ParametrageProcessDacRespId {
    private RefParamResponseDto typeMarcher;
    private ModeDePassationSelection modePassation;
}
