package com.sigmap.common.agpm.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;


import java.util.List;
@Data
public class AgpmResponseDto {

    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String contenu;
    private String remarque;
    private String status;
    private String annee;
    private AcResponseDto ac;
    private List<DocsResponseDTO> docs;
    private String processInstanceId;
}
