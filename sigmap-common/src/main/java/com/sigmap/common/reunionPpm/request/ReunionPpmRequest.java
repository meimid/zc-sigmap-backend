package com.sigmap.common.reunionPpm.request;

import lombok.Data;

@Data
public class ReunionPpmRequest {
    private Integer id;
    private Integer version;
    private String processInstanceId;
    private String taskId;
    private String decision;
    private String observation;
}
