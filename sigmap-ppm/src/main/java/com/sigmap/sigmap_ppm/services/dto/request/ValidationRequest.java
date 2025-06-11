package com.sigmap.sigmap_ppm.services.dto.request;

import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import lombok.Data;

@Data
public class ValidationRequest {
    private Boolean validate;
    private String comment;
    private PpmWithLotsRequestDto ppm;
    private Integer comId;
}