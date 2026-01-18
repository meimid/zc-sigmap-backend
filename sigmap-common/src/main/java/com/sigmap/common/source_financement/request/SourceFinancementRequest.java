package com.sigmap.common.source_financement.request;

import lombok.Data;

@Data
public class SourceFinancementRequest {
    private String code;

    private String description;

    private Boolean bailleurDeFonds;
}
