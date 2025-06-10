package com.sigmap.sigmap_ppm.services.dto.response;


import com.sigmap.common.ppms.response.PpmResponseDto;
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
    private PpmResponseDto ppm;
    
    // Status information
    private String status;

    private boolean updatable;
    private boolean isPublishable;
    private boolean commissionRequired;
    private Boolean hasTask;
    // Next steps information
    private List<NextStepDTO> nextSteps;




}