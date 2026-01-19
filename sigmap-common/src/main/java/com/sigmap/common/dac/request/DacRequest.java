package com.sigmap.common.dac.request;

import lombok.Data;

@Data
public class DacRequest {
   // @NotNull(message = "DAC ID cannot be null")
    private Integer dacId;

    //@NotNull(message = "DAC Version cannot be null")
    private Integer dacVer;

    private Integer dynaDocsVer;
    private Integer dynaDocsId;
    private Boolean generateDocx;
}
