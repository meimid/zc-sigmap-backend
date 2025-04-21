package com.sigmap.common.ref_params.response;

import com.sigmap.common.ref_types.RefType;

import com.sigmap.common.ref_types.response.RefTypeResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

/**
 * A Response object for the {@link RefParam} entity.
 */
@Data
public class RefParamResponse {
    private Integer id;

    private String code;

    private String description;

    private RefTypeResponseDto refType;

    private BaseEntityResponse metadata;


}
