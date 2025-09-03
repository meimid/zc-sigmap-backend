package com.sigmap.common.reunionPpm.response;

import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.ppms.response.PpmIdVerNomResponseSoftDto;
import com.sigmap.common.ppms.response.PpmResponseDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReunionPpmResponse {
    private PpmIdVerNomResponseSoftDto ppm;
    private String processInstanceId;
    private String taskId;
    private String decision;
    private String observation;
    private List<DocsResponseDTO> docs=new ArrayList<>();
}

