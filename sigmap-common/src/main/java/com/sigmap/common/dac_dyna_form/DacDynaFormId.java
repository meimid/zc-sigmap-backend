package com.sigmap.common.dac_dyna_form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DacDynaFormId implements Serializable {
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaFormId;
    private Integer dynaFormVer;
    private Integer dynaFormFieldsId;
}
