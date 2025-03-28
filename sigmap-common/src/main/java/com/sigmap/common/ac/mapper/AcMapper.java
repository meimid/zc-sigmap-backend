package com.sigmap.common.ac.mapper;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.response.AcResponse;
import com.sigmap.common.ref_params.response.RefParamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
@Mapper
public interface AcMapper {
    AcMapper INSTANCE = Mappers.getMapper(AcMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AcResponse map(Ac source);
}
