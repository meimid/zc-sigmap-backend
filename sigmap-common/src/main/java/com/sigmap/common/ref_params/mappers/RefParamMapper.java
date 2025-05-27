package com.sigmap.common.ref_params.mappers;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_params.request.RefParamRequestWithoutRefTypeIdDto;
import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Use MapStruct to generate code for mapping between Objects.
 *
 * @see <a href="https://mapstruct.org/documentation/stable/reference/html/">MapStruct Documentation</a>
 */

@Mapper
public interface RefParamMapper {
    RefParamMapper INSTANCE = Mappers.getMapper(RefParamMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    RefParamResponse map(RefParam source);
    RefParam map(RefParamRequest source);
    void updateFromRequest(RefParamRequest source, @MappingTarget RefParam target);
    RefParamResponseDto mapToDto(RefParam source);
    RefParamRequest mapToRequest(RefParamRequestWithoutRefTypeIdDto source);
}
