package com.sigmap.common.reunionDac.response;

import com.sigmap.common.dac.response.DacResponseReunion;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReunionDacResponse {
    private DacResponseReunion dac;
    private String processInstanceId;
    private String taskId;
    private String decision;
    private String observation;
    private List<DocsResponseDTO> docs=new ArrayList<>();


}
