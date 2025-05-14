package com.sigmap.sigmap_agpm.service.dto.response;

import lombok.Data;

@Data
public class NextStepDTO {
    private String validationVariable;
    private Boolean validationValue;
    private String nextTaskName;
    private String actionLabel;
}
