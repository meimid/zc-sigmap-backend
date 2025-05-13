package com.sigmap.common.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TaskDTO {
    // Common fields
    private String taskId;
    private String taskName;
    private String taskDefinitionKey;
    private String assignee;
    private String processInstanceId;
    private LocalDateTime created;
    private String candidateGroup;
    private Map<String, Object> traceability;
    
    // entity related fields
    private Integer id;
    private Integer version;
    private Integer newStatus;
    private Boolean isValid;
    private String status;
    private Boolean isUpdatable;
    private Boolean isPublishable;

    // Next steps information
    private List<NextStepDTO> nextSteps;
}