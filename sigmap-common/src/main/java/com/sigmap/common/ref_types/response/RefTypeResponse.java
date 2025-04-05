package com.sigmap.common.ref_types.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;
/**
 * A Response object for the {@link RefType} entity.
 */
@Data
public class RefTypeResponse {
    private Integer id;
    private String description;
    private BaseEntityResponse metadata;
}
