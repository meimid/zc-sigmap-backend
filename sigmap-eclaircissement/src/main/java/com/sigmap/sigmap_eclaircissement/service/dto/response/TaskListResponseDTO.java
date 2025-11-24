package com.sigmap.sigmap_eclaircissement.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskListResponseDTO {
    private String tenantId;
    private List<TaskDTO> tasks;
    private String candidateGroup;
    private String status;
}