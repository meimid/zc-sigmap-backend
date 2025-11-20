package com.sigmap.common.ac.mapper;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.ac.response.*;
import com.sigmap.common.com.mapper.ComMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AcMapper {
    AcMapper INSTANCE = Mappers.getMapper(AcMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AcResponse map(Ac source);
    AcResponseDto mapToDto(Ac source);
    AcAvailablePaaResponse mapToAcAvailablePaaResponse(Ac source);
    Ac map(AcRequest source);
    void updateAcFromRequest(AcRequest source, @MappingTarget Ac target);
    List<AcWebsiteResponse> mapToWebsiteResponse(List<Ac> source);
    List<AcSelectResponseDto> mapToSelectDto (List<Ac> source);
    AcWebsiteResponse mapToWebsiteResponse(Ac source);


}
