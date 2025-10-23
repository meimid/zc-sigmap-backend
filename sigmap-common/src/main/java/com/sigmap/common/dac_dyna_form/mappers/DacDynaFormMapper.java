package com.sigmap.common.dac_dyna_form.mappers;

import com.sigmap.common.dac_dyna_form.DacDynaForm;
import com.sigmap.common.dac_dyna_form.request.DacDynaFormRequest;
import com.sigmap.common.dac_dyna_form.response.DacDynaFormResponse;
import com.sigmap.common.dyna_form.DynaForm;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between DacDynaForm entities and their DTOs.
 * Used for CRUD operations (assign, update display, detach).
 */
@Mapper(componentModel = "spring")
public interface DacDynaFormMapper {
    DacDynaFormMapper INSTANCE = Mappers.getMapper(DacDynaFormMapper.class);

    /**
     * Converts a request into an entity.
     * No auto-generated fields — IDs are fully provided in the request.
     */
    @Mapping(target = "dacId", source = "dacId")
    @Mapping(target = "dacVer", source = "dacVer")
    @Mapping(target = "dynaFormId", source = "dynaFormId")
    @Mapping(target = "dynaFormVer", source = "dynaFormVer")
    @Mapping(target = "dynaFormFieldsId", ignore = true)
    @Mapping(target = "display", source = "display")
    DacDynaForm toEntity(DacDynaFormRequest request);

    /**
     * Converts an entity to its response DTO.
     */
    @Mapping(target = "dacId", source = "dacId")
    @Mapping(target = "dacVer", source = "dacVer")
    @Mapping(target = "dynaFormId", source = "dynaFormId")
    @Mapping(target = "dynaFormVer", source = "dynaFormVer")
    @Mapping(target = "dynaFormFieldsId", source = "dynaFormFieldsId")
    @Mapping(target = "display", source = "display")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    DacDynaFormResponse toResponse(DacDynaForm entity);

    default DynaFormResponse toDynaFormResponse(DynaForm form) {
        DynaFormResponse res = new DynaFormResponse();
        res.setId(form.getId());
        res.setVersion(form.getVersion());
        res.setName(form.getName());
        res.setTypeDossier(form.getTypeDossier());
        res.setFormType(form.getFormType());
        res.setStatus(form.getStatus());
        res.setOrder(form.getOrder());
        return res;
    }
}
