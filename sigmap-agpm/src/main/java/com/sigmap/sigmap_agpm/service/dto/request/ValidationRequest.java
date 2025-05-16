package com.sigmap.sigmap_agpm.service.dto.request;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.request.AgpmRequest;
import lombok.Data;

@Data
public class ValidationRequest {
    private Boolean validate;
    private String comment;
    private AgpmRequest agpm;
}