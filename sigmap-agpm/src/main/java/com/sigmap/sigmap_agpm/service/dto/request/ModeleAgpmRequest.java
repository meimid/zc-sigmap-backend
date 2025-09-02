package com.sigmap.sigmap_agpm.service.dto.request;

import lombok.Data;

@Data
public class ModeleAgpmRequest {
    private Integer agpmId;
    private Integer agpmVer;
    private String signataire;
    private String fonction;
}
