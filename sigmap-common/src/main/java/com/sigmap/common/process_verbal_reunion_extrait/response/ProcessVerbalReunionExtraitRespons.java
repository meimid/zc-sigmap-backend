package com.sigmap.common.process_verbal_reunion_extrait.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.ac.response.AcWebsiteResponse;
import com.sigmap.common.dac.response.DacIdVerRefResponseDto;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.entity.ExtraitType;
import com.sigmap.common.ppms.response.PpmIdVerResponseDto;
import com.sigmap.common.ppms.response.PpmResponseSoftDto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalReunionExtraitRespons {

    private Integer extraitId;
    private Integer pvId;
    private AcWebsiteResponse ac;
    private DocsResponseDTO doc;
    private ExtraitType extraitType;
    /*private PpmIdVerResponseDto ppm;
    private DacIdVerRefResponseDto dac;*/
    private DossierDto dossier;
    private String type;


}