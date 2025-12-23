package com.sigmap.sigmap_dac.services.dto.response;


import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionCreateRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.dac_dyna_form.request.DacDynaFormRequest;
import com.sigmap.common.dac_prix.response.DacPrixResponce;
import com.sigmap.common.ppms.response.PpmResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TaskDetailsDTO {
    // Task information
    private String taskId;
    private String taskName;
    private String taskDefinitionKey;
    private String assignee;
    private String processInstanceId;
    private LocalDateTime created;
    private String candidateGroup;
    private Map<String, Object> traceability;
    
    private DacResponseDTO dacDocs;
    // prix dac information
    private List<DacPrixResponce> dacPrix;;

    // Status information
    private String status;

    private Boolean isUpdatable;
    private Boolean isPublishable;
    private boolean commissionRequired;
    private Boolean meetingRequired;
    private Integer comId;
    private Boolean hasTask;
    // Next steps information
    private List<NextStepDTO> nextSteps;




}