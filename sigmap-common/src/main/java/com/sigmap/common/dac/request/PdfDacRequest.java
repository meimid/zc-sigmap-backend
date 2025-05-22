package com.sigmap.common.dac.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PdfDacRequest {
   // @NotBlank(message = "Document path cannot be empty")
  //  private String docPath;

    //@NotNull(message = "DAC ID cannot be null")
    private Integer dacId;

    //@NotNull(message = "DAC version cannot be null")
    private Integer dacVer;
}
