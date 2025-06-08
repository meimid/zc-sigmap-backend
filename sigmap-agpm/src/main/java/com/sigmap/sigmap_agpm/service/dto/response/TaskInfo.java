package com.sigmap.sigmap_agpm.service.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class TaskInfo {

    private String taskId;
    private String taskName;
    private Date createTime;
    private String assignee;
    private String processInstanceId;
    private String taskDefKey;
    private String tenantId;
    private String candidateGroup;
    private Integer acId;
}
