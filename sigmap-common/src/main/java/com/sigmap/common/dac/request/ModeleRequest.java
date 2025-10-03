package com.sigmap.common.dac.request;

import lombok.Data;

@Data
public class ModeleRequest {
    private Integer dacId;
    private Integer dacVersion;
    private String signataire;
    private String fonction;
}
