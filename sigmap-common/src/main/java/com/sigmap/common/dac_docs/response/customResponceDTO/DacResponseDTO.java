package com.sigmap.common.dac_docs.response.customResponceDTO;

import lombok.Data;

import java.util.List;

@Data
public class DacResponseDTO {
    private Integer dacId;
    private Integer dacVer;
    private DacPpmResponseDTO ppm;
    private DacTypeMarcheResponseDTO typeMarche;
    private DacRealisationResponseDTO realisation;
    private List<DynaDocsResponseDTO> dynaDocs;
}
