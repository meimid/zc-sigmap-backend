package com.sigmap.common.docs.Mapper;

import com.sigmap.common.docs.Docs;

import com.sigmap.common.docs.Response.DocsResponse;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
@Mapper
public interface DocMapper {
    DocMapper INSTANCE = Mappers.getMapper(DocMapper.class);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    DocsResponse map(Docs source);
    DocsResponseDTO mapToDto(Docs source);

}
