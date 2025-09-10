package com.sigmap.sigmap_dac.services.dto.response;

import lombok.Data;

@Data
public class NextStepDTO {
    private String validationVariable;
    private String validationValue;
    private String nextTaskName;
    private String actionLabel;
}
