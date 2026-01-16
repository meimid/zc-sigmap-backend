package com.sigmap.common.source_financement.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

/**
 * A Response object for the {@link SourceFinancement} entity.
 */
@Data
public class SourceFinancementResponse {
    private Integer id;

    private String code;

    private String description;

    private Boolean bailleurDeFonds;

    private BaseEntityResponse metadata;
}
