package com.sigmap.common.dyna_form.response;

import com.sigmap.common.dyna_form_fields.response.DynaFormFieldResponse;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.TypeDossier;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DynaFormResponse {
    private Integer id;
    private Integer version;
    private String name;
    private Integer order;
    private TypeDossier typeDossier;
    private ActiveStatus status;
    private List<DynaFormFieldResponse> fields = new ArrayList<>();
    private BaseEntityResponse metadata;

}
