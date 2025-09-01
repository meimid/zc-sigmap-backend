package com.sigmap.sigmap_paa.service.dto.request;

import lombok.Data;

@Data
public class ModelePaaRequest {
    private Integer paaId;
    private Integer paaVer;
    private String signataire;
    private String fonction;
}
