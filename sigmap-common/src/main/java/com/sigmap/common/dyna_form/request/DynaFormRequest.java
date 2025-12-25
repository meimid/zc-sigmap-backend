package com.sigmap.common.dyna_form.request;

import com.sigmap.common.dyna_form_fields.request.DynaFormFieldRequest;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.FormType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DynaFormRequest {
    private String name;
    private Integer order;
    private Integer typeDossierId;
    private ActiveStatus status;
    private FormType formType;
    private List<DynaFormFieldRequest> fields = new ArrayList<>();

}
