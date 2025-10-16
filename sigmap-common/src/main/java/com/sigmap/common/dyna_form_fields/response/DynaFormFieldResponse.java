package com.sigmap.common.dyna_form_fields.response;

import lombok.Data;

@Data
public class DynaFormFieldResponse {
    private Integer dynaFormFieldsId;
    private String name;
    private String label;
    private String description;
    private Integer typeId;
    private Boolean isRequired;
    private Integer order;
}
