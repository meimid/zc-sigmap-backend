package com.sigmap.sigmap_agpm.agpm.response;

import lombok.Data;

@Data
public class NextStepDTO {
    private String validationVariable;
    private Boolean validationValue;
    private String nextTaskName;
    private String actionLabel;
}
