package com.sigmap.common.reunionDac.response;

import com.sigmap.common.dac_docs.response.DacDocsResponse;
import lombok.Data;

@Data
public class ReunionDacResponse {
    private DacDocsResponse dac;
    private String processInstanceId;
    private String taskId;

}
