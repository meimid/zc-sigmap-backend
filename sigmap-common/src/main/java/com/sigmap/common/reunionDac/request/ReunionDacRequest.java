package com.sigmap.common.reunionDac.request;

import lombok.*;

@Data
public class ReunionDacRequest {
    private Integer dacId;
    private Integer dacVer;
    private String processInstanceId;
    private String taskId;
}
