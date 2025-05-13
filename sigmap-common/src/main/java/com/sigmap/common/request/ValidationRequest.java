package com.sigmap.common.request;

import com.sigmap.common.ppms.request.PpmRequest;
import lombok.Data;

@Data
public class ValidationRequest {
    private Boolean isValid;
    private String comment;
    private PpmRequest ppm;
}