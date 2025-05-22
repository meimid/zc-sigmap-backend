package com.sigmap.common.response;

import lombok.Data;

@Data
public class NextStepDTO {

        private String validationVariable; // e.g., "reserve_valide"
        private Boolean validationValue;   // true or false, null for single-outcome tasks
        private String nextTaskName;      // e.g., "CPMP" or "anotherTask"
        private String actionLabel;       // e.g., "Validate", "Reject", or "Complete"

}
