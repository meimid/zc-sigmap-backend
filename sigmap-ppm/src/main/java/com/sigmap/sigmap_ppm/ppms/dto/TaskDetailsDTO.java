package com.sigmap.sigmap_ppm.ppms.dto;

import com.sigmap.common.ppms.response.PpmResponse;
import com.sigmap.common.response.NextStepDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TaskDetailsDTO {
    // Task information
    private String taskId;
    private String taskName;
    private String taskDefinitionKey;
    private String assignee;
    private String processInstanceId;
    private LocalDateTime created;
    private String candidateGroup;
    private Map<String, Object> traceability;
    
    // PPM entity information
    private List<PpmResponse> ppms;
    
    // Status information
    private String status;

    private boolean updatable;
    private boolean isPublishable;

    // Next steps information
    private List<NextStepDTO> nextSteps;




}