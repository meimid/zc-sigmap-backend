package com.sigmap.common.dac.response;

import com.sigmap.common.ac.response.AcWebsiteResponse;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class DacResponseReunion {
    private Integer dacId;
    private Integer dacVer;
    private RefParamResponseDto typeMarche;
    private ProcessStatus status;
    private AcWebsiteResponse ac;
}
