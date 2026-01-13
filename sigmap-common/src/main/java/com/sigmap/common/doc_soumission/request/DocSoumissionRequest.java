package com.sigmap.common.doc_soumission.request;

import lombok.Data;

@Data
public class DocSoumissionRequest {
    private String nom;
    private Integer typeDossierId;
    private Boolean validationDateRequired;


}
