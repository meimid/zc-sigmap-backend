package com.sigmap.common.dyna_form_fields.mappers;

import com.sigmap.common.dyna_form.request.DynaFormRequest;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import com.sigmap.common.dyna_form_fields.DynaFormField;
import com.sigmap.common.dyna_form_fields.request.DynaFormFieldRequest;
import com.sigmap.common.dyna_form_fields.response.DynaFormFieldResponse;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DynaFormFieldMapper {
    DynaFormFieldMapper INSTANCE = Mappers.getMapper(DynaFormFieldMapper.class);

    @Mapping(target = "dynaFormFieldsId",   source = "dynaFormFieldsId")
    @Mapping(target = "name",      source = "name")
    @Mapping(target = "label",     source = "label")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "isRequired",source = "isRequired")
    @Mapping(target = "order",     source = "order")
    @Mapping(target = "hasTotal",         source = "hasTotal")
    @Mapping(target = "isFormula",        source = "isFormula")
    @Mapping(target = "formula",          source = "formula")
    @Mapping(target = "status",          source = "status")
    DynaFormFieldResponse toResponse(DynaFormField entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "dynaFormFieldsId",   source = "dynaFormFieldsId")
    @Mapping(target = "name",       source = "name")
    @Mapping(target = "label",      source = "label")
    @Mapping(target = "description",source = "description")
    @Mapping(target = "type",     source = "type")
    @Mapping(target = "isRequired", source = "isRequired")
    @Mapping(target = "order",      source = "order")
    @Mapping(target = "hasTotal",         source = "hasTotal")
    @Mapping(target = "isFormula",        source = "isFormula")
    @Mapping(target = "formula",          source = "formula")
    @Mapping(target = "status",          source = "status")
    DynaFormField toEntity(DynaFormFieldRequest req);
}

