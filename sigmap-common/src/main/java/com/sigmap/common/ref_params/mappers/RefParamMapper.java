package com.sigmap.common.ref_params.mappers;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RefParamMapper {
    RefParamMapper INSTANCE = Mappers.getMapper(RefParamMapper.class);

    RefParamResponse map(RefParam source);

}
