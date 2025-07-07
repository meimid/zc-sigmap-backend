package com.sigmap.common.dac_docs.response.customResponceDTO;

import lombok.Data;

import java.util.List;

@Data
public class DacDocResponseDTO {
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaDocId;
    private Integer dynaDocVersion;
    private Integer dynaFieldId;
    private String sourceId;
    private String fieldValue;
    private List<DacDocDetailsDTO> dacDocDetails;
}
