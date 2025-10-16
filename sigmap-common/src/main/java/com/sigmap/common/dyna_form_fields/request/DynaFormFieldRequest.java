package com.sigmap.common.dyna_form_fields.request;

import lombok.Data;

@Data
public  class DynaFormFieldRequest {
    private Integer fieldId;
    private String name;
    private String label;
    private String description;
    private Integer typeId;
    private Boolean isRequired;
    private Integer order;
}
