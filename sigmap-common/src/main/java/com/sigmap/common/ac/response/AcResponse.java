package com.sigmap.common.ac.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class AcResponse {
    private Integer id;
    private String nom;
    private String ancrage;
    private String contact;
    private BaseEntityResponse metadata;

}
