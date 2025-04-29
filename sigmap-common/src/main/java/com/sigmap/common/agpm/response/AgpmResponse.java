package com.sigmap.common.agpm.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for returning Agpm data.
 * Includes full Ac and Docs objects for use in the application.
 */
@Data
public class AgpmResponse {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String contenu;
    private String remarque;
    private RefParamResponseDto status;
    private AcResponseDto ac;
    private List<DocsResponseDTO> docs;
    private BaseEntityResponse metadata;

}