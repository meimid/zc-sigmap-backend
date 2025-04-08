package com.sigmap.common.com.mapper;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.com.response.ComResponse;
import com.sigmap.common.com.response.ComResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ComMapper {
    ComMapper INSTANCE = Mappers.getMapper(ComMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    ComResponse map(Com source);
    Com map(ComRequest source);
    List<ComResponse> map(List<Com> source);
}
