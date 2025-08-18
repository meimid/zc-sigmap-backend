package com.sigmap.common.dac_docs_details.request;

import lombok.Data;

@Data
public class DacDocsDetailsRequest {
    private Integer dynaFieldsDetailsId;
    private String sourceId;
    private String fieldValue;
    private Integer rowId;
}
