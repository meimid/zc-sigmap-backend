package com.sigmap.common.response;

import lombok.Data;

import java.util.List;

@Data
public class TaskListResponseDTO {
    private String tenantId;
    private List<TaskDTO> tasks;
    private String candidateGroup;
    private String status;
} 