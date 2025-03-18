package com.sigmap.common.ref_params.mappers;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Use MapStruct to generate code for mapping between Objects.
 *
 * @see <a href="https://mapstruct.org/documentation/stable/reference/html/">MapStruct Documentation</a>
 */

@Mapper
public interface RefParamMapper {
    RefParamMapper INSTANCE = Mappers.getMapper(RefParamMapper.class);

    RefParamResponse map(RefParam source);
}
