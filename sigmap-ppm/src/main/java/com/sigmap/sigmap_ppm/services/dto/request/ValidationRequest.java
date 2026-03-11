package com.sigmap.sigmap_ppm.services.dto.request;

import com.sigmap.common.config.CncmpDecision;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import lombok.Data;

@Data
public class ValidationRequest {
    private Boolean validate;
    private CncmpDecision cncmpDecision;
    private String comment;
    private PpmWithLotsRequestDto ppm;
    private Integer comId;
    private Boolean fromMeeting;
    private String assigneeUserId;
}