package com.sigmap.sigmap_ppm.services.dto.response;

import lombok.Data;

@Data
public class NextStepDTO {
    private String validationVariable;
    private Boolean validationValue;
    private String nextTaskName;
    private String actionLabel;
}
