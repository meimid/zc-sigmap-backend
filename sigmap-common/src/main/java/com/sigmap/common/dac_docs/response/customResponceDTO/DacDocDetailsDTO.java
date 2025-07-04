package com.sigmap.common.dac_docs.response.customResponceDTO;

import lombok.Data;

import java.util.List;

@Data
public class DacDocDetailsDTO {

    private Integer dynaFieldsDetailsId;
    private Integer rowId;
    private String sourceId;
    private String fieldValue;
    private List<DynaDocsResponseDTO> dynaDocs;
}
