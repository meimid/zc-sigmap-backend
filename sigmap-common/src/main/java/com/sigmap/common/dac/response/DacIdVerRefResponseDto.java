package com.sigmap.common.dac.response;

import lombok.Data;

@Data
public class DacIdVerRefResponseDto {
    private Integer dacId;

    private Integer dacVer;

    private String reference;

}
