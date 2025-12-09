package com.sigmap.common.ppms.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class PpmResponseDto {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private AcResponseDto ac;
    private BigDecimal budgetEstime;
    private String status;
    private RefParamResponseDto modePassation;
    private String remarque;
    private List<DocsResponseDTO> docs;
    private String modelAlfrescoId;
    private List<RealisationResponseDTO> realisations;
    private String processInstanceId;
    private Boolean actualise;
    private Integer nbrActualisation;
    private String etapeProcess;
    private List<String> pvCpmp;
    private List<String> pvCncmpPermanente;
    private List<String> pvCncmpSpecialisee;
    private List<String> extraitPvs;
    private BaseEntityResponse metadata;
}
