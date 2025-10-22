package com.sigmap.common.dac_dyna_form.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class DacDynaFormResponse {
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaFormId;
    private Integer dynaFormVer;
    private Integer dynaFormFieldsId;
    private Boolean display;
    private BaseEntityResponse metadata;
}
