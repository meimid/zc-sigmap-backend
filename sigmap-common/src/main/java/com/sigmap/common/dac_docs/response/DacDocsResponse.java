package com.sigmap.common.dac_docs.response;

import lombok.Data;

@Data
public class DacDocsResponse {
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaFieldsId;
    private Integer dynaDocsId;
    private Integer dynaDocsVer;
    private String fieldValue;
    private String sourceId;

} 