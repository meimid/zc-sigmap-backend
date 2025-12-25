// com.sigmap.common.dyna_form.mappers.DynaFormMapper
package com.sigmap.common.dyna_form.mappers;

import com.sigmap.common.dyna_form.DynaForm;
import com.sigmap.common.dyna_form.request.DynaFormRequest;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DynaFormMapper {

    DynaFormMapper INSTANCE = Mappers.getMapper(DynaFormMapper.class);

    /**
     * Maps a DynaFormRequest to a DynaForm header entity.
     * ID and version are ignored since they’re generated in the service layer.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "formType", source = "formType")
    @Mapping(target = "fields", ignore = true)
    DynaForm toHeaderEntity(DynaFormRequest request);

    /**
     * Maps a DynaForm entity to its response DTO.
     */
    @Mapping(target = "formType", source = "formType")
    @Mapping(target = "fields", source = "fields")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    DynaFormResponse toResponse(DynaForm entity);

    /**
     * Utility: checks if the header-level properties changed (used in versioning logic).
     */
    default boolean headerChanged(DynaForm latest, DynaFormRequest req) {
        if (req == null) return false;
        boolean nameChanged  = !eq(latest.getName(), req.getName());
        boolean orderChanged = !eq(latest.getOrder(), req.getOrder());
        boolean typeChanged  = latest.getTypeDossier().getId() != req.getTypeDossierId();
        boolean formTypeChanged = latest.getFormType() != req.getFormType();
        boolean statusChanged   = latest.getStatus() != req.getStatus();
        return nameChanged || orderChanged || typeChanged || formTypeChanged || statusChanged;
    }

    /**
     * Utility: creates a copy of the header (used when duplicating).
     */
    default DynaForm copyHeader(DynaForm src) {
        DynaForm d = new DynaForm();
        d.setName(src.getName());
        d.setOrder(src.getOrder());
        d.setTypeDossier(src.getTypeDossier());
        d.setFormType(src.getFormType());
        d.setStatus(src.getStatus());
        return d;
    }

    private static boolean eq(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
