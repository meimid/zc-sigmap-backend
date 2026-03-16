package com.sigmap.common.ppms.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.ppm_avis_note_rapporteur.response.PpmAvisNoteRapporteurResponse;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class PpmResponseSoftDto {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String annee;
    private AcResponseDto ac;
    private BigDecimal budgetEstime;
    private String status;
    private RefParamResponseDto modePassation;
    private Boolean actualise;
    private Integer nbrActualisation;
    private String remarque;
    private String etapeProcess;
    private List<String> pvCpmp;
    private List<String> pvCncmpPermanente;
    private List<String> pvCncmpSpecialisee;
    private List<String> extraitCncmpPermanente;
    private Set<PpmAvisNoteRapporteurResponse> avis =new HashSet<>();
    private List<String> extraitCpmp;
}
