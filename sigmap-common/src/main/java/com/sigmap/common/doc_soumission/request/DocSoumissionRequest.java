package com.sigmap.common.doc_soumission.request;

import com.sigmap.common.config.TypeDossier;
import lombok.Data;

@Data
public class DocSoumissionRequest {
    private String nom;
    private TypeDossier typeDossier;

}
