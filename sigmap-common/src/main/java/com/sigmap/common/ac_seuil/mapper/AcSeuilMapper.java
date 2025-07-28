package com.sigmap.common.ac_seuil.mapper;

import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ac_seuil.request.AcSeuilRequest;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AcSeuilMapper {
    AcSeuilMapper INSTANCE = Mappers.getMapper(AcSeuilMapper.class);

   // AcSeuil toEntity (AcSeuilRequest request);
}
