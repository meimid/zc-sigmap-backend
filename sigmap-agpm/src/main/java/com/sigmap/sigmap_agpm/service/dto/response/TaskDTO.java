package com.sigmap.sigmap_agpm.service.dto.response;

import com.sigmap.common.entity.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    private String taskId;
    private String taskName;
    private String taskDefinitionKey;
    private String assignee;
    private String processInstanceId;
    private LocalDateTime created;
    private String candidateGroup;
    private Map<String, Object> traceability;
    private Integer id;
    private Integer version;
    private String nom;
    private String ref;
    private String modelAlfrescoId;
    private ProcessStatus newStatus;
    private Boolean validate;
    private String status;
    private Boolean isUpdatable;
    private Boolean isPublishable;
}