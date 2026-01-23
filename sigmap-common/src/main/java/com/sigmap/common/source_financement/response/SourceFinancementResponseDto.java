package com.sigmap.common.source_financement.response;

import lombok.Data;

/** A lightweight Response object for the {@link SourceFinancement} entity. */
@Data
public class SourceFinancementResponseDto {
    private Integer id;

    private String code;

    private String description;

    private Boolean bailleurDeFonds;
}
