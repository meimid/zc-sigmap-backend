package com.sigmap.common.reunionDac.response;

import com.sigmap.common.dac.response.DacResponseReunion;
import lombok.Data;

@Data
public class ReunionDacResponse {
    private DacResponseReunion dac;
    private String processInstanceId;
    private String taskId;

}
