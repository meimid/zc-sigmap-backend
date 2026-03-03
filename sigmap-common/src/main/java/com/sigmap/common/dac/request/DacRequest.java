package com.sigmap.common.dac.request;

import lombok.Data;

@Data
public class DacRequest {
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaDocsVer;
    private Integer dynaDocsId;
    private Boolean generateDocx;
}
