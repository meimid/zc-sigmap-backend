package com.sigmap.common.reunionPpm.response;

import com.sigmap.common.ppms.response.PpmIdVerNomResponseSoftDto;
import com.sigmap.common.ppms.response.PpmResponseDto;
import lombok.Data;

@Data
public class ReunionPpmResponse {
    private PpmIdVerNomResponseSoftDto ppm;
    private String processInstanceId;
    private String taskId;
}

