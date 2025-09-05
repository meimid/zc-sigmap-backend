package com.sigmap.common.dac_docs_details.response;

import lombok.Data;

@Data
public class DacDocsDetailsResponse {
    private Integer dynaFieldsDetailsId;
    private Integer rowId;
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaDocsId;
    private Integer dynaDocsVer;
    private Integer dynaFieldsId;
    private String sourceId;
    private String fieldValue;
} 