package com.sigmap.sigmap_agpm.service.dto.response;

import com.sigmap.common.agpm.response.AgpmResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDetailsDTO {
    private String taskId;
    private String taskName;
    private String taskDefinitionKey;
    private String assignee;
    private String processInstanceId;
    private LocalDateTime created;
    private String candidateGroup;
    private Map<String, Object> traceability;
    private List<AgpmResponseDto> agpms;
    private String status;
    private Boolean isUpdatable;
    private Boolean isPublishable;
    private List<NextStepDTO> nextSteps;
}