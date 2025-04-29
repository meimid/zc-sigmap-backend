package com.sigmap.common.ref_params.response;

import com.sigmap.common.ref_types.RefType;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

/**
 * A Response object for the {@link } entity.
 */
@Data
public class RefParamResponseDto {
    private Integer id;

    private String code;

    private String description;

}
