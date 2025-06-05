package com.sigmap.common.ppms.response;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PpmResponse {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private Ac ac;
    private BigDecimal budgetEstime;
    private RefParamResponseDto status;
    private RefParamResponseDto modePassation;
    private List<DocsResponseDTO> docs;
    private List<RealisationResponseDTO> realisations;
    private String processInstanceId;
    private BaseEntityResponse metadata;

}