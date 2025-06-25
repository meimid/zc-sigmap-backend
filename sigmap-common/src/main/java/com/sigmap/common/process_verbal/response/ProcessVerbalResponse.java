package com.sigmap.common.process_verbal.response;

import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ppms.request.PpmBaseRequestDto;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.pv_docs.response.PvDocsResponseDto;
import com.sigmap.common.pv_membre_present.response.PvMembrePresentResponseDto;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalResponse {
    private String processVerbalId;
    private PpmResponseDto ppm;
    private Set<PvMembrePresentResponseDto> membres;
    private LocalDateTime dateDebutReunion;
    private String observation;
    private boolean decision;
    private RefParamResponseDto typeId;
    private ProcessStatus status;
    private Set<PvDocsResponseDto> documents;
    private BaseEntityResponse metadata;

}
