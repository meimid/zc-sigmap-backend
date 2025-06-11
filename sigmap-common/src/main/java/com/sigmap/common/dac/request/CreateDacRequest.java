package com.sigmap.common.dac.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDacRequest {

    private Integer dacVer;
    private Integer ppmId;
    private Integer ppmVer;
    private Integer categoryId;
    private Integer statusId;
}
