package com.sigmap.common.process_verbal_reunion_extrait.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.ac.response.AcWebsiteResponse;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.entity.ExtraitType;
import com.sigmap.common.ppms.response.PpmIdVerResponseDto;
import com.sigmap.common.ppms.response.PpmResponseSoftDto;
import lombok.Data;

@Data
public class ProcessVerbalReunionExtraitRespons {

    private Integer extraitId;
    private Integer pvId;
    private PpmIdVerResponseDto ppm;
    private AcWebsiteResponse ac;
    private DocsResponseDTO doc;
    private ExtraitType extraitType;

}