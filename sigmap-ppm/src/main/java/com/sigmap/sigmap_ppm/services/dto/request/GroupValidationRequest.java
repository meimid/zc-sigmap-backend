package com.sigmap.sigmap_ppm.services.dto.request;

import com.sigmap.common.config.CncmpDecision;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import lombok.Data;

@Data
public class GroupValidationRequest {
    private String taskId;
    private Boolean validate;
    private CncmpDecision cncmpDecision;
    private String comment;
    private PpmWithLotsRequestDto ppm;
    private Integer comId;
}
