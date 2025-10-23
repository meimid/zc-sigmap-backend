package com.sigmap.common.dyna_form_fields.response;

import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.entity.ActiveStatus;
import lombok.Data;

@Data
public class DynaFormFieldResponse {
    private Integer dynaFormFieldsId;
    private String name;
    private String label;
    private String description;
    private DynaFieldType type;
    private Boolean isRequired;
    private Integer order;
    private Boolean hasTotal;
    private Boolean isFormula;
    private String formula;
    private ActiveStatus status;
}
