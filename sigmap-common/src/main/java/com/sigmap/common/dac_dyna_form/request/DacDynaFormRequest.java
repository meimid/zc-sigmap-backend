package com.sigmap.common.dac_dyna_form.request;

import lombok.Data;

import java.util.List;

@Data
public class DacDynaFormRequest {
    private Integer dacId;
    private Integer dynaFormId;
    private Integer dynaFormVer;
    private List<Integer> dynaFormFieldsId;
    private Boolean display;
}
