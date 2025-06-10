package com.sigmap.sigmap_agpm.service.dto.response;

import lombok.Data;

@Data
public class ProcessStartResponseDTO {
    private String processInstanceId;
    private String processDefinitionId;
    private String taskId;
    private String status;
    private Integer acId;
}