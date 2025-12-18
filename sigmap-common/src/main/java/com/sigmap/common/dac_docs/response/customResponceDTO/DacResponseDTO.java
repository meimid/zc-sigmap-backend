package com.sigmap.common.dac_docs.response.customResponceDTO;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.dac.response.DacWithAoReferenceResponse;
import com.sigmap.common.dac_prix.response.DacPrixResponce;
import com.sigmap.common.entity.ProcessStatus;
import lombok.Data;

import java.util.Calendar;
import java.util.List;

@Data
public class DacResponseDTO {
    private Integer dacId;
    private Integer dacVer;
    private String reference;
    private Calendar dateLimiteSoumission;
    private Calendar dateLimiteRecptionDemande;
    private boolean payant;
    private List<DacPrixResponce> dacPrix;
    private List<DacWithAoReferenceResponse.ShortlistMemberData>  dacShortlist;
    private ProcessStatus status;
    private DacPpmResponseDTO ppm;
    private DacTypeMarcheResponseDTO typeMarche;
    private ModeDePassationSelection modePassation;
    private DacRealisationResponseDTO realisation;
    private List<DynaDocsResponseDTO> dynaDocs;
    private String etapeProcess;
    private String etapeDac;
    private String conditionsParticipation;
    private String responsableDacId;
}
