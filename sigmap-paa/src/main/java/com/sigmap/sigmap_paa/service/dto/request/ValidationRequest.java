package com.sigmap.sigmap_paa.service.dto.request;


import com.sigmap.common.paas.request.PaaRequestGlobal;
import lombok.Data;

@Data
public class ValidationRequest {
    private Boolean validate;
    private String comment;
    private PaaRequestGlobal paa;
}