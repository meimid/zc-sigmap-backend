package com.sigmap.common.doc_soumission.response;

import com.sigmap.common.config.TypeDossier;
import lombok.Data;

@Data
public class DocSoumissionResponse {
    private Integer id;
    private String nom;
    private TypeDossier typeDossier;
}
