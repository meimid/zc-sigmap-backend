package com.sigmap.common.dyna_form_fields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynaFormFieldId implements Serializable {
    private Integer dynaFormId;
    private Integer dynaFormVer;
    private Integer dynaFormFieldsId;
}
