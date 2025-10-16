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
    @Mapping(target = "typeId",    source = "typeId")
    @Mapping(target = "isRequired",source = "isRequired")
    @Mapping(target = "order",     source = "order")
    DynaFormFieldResponse toResponse(DynaFormField entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name",       source = "name")
    @Mapping(target = "label",      source = "label")
    @Mapping(target = "description",source = "description")
    @Mapping(target = "typeId",     source = "typeId")
    @Mapping(target = "isRequired", source = "isRequired")
    @Mapping(target = "order",      source = "order")
    DynaFormField toEntity(DynaFormFieldRequest req);
}

