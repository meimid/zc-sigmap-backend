package com.sigmap.common.process_verbal_reunion_extrait.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.ac.response.AcWebsiteResponse;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import lombok.Data;

@Data
public class ProcessVerbalReunionExtraitRespons {

    private Integer extraitId;
    private Integer pvId;
    private AcWebsiteResponse ac;
    private DocsResponseDTO doc;
}