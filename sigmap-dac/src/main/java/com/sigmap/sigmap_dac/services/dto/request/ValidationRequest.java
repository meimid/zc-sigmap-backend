package com.sigmap.sigmap_dac.services.dto.request;

import com.sigmap.common.config.CncmpDecision;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.request.DacWithDacDocsRequest;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import lombok.Data;

import java.util.List;

@Data
public class ValidationRequest {
    private Boolean validate;
    private CncmpDecision cncmpDecision;
    private String comment;
    private DacWithDacDocsRequest dacWithDacDocsRequest;
    private Integer comId;
    private Boolean fromMeeting;
}