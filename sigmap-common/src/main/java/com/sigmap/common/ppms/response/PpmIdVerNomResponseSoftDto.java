package com.sigmap.common.ppms.response;

import lombok.Data;

@Data
public class PpmIdVerNomResponseSoftDto {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
}
