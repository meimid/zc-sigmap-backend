package com.sigmap.sigmap_ppm.services.dto.request;

import lombok.Data;

@Data
public class ModeleRequest {
    private Integer ppmId;
    private Integer ppmVer;
    private String signataire;
    private String fonction;
}
