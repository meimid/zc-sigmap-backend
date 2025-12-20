package com.sigmap.common.dac_docs.response.customResponceDTO;

import lombok.Data;

@Data
public class DacPpmResponseDTO {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private Integer acId;
    private String etapeProcess;
}
