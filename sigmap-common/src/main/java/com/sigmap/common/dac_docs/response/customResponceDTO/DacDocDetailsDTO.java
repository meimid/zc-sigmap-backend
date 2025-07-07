package com.sigmap.common.dac_docs.response.customResponceDTO;

import lombok.Data;

@Data
public class DacDocDetailsDTO {
    private Integer dynaFieldsDetailsId;
    private Integer rowId;
    private String sourceId;
    private String fieldValue;
    private Integer dynaDocId;
    private Integer dynaDocVersion;
    private Integer dynaFieldId;
}
