package com.sigmap.common.doc_soumission.response;

import com.sigmap.common.type_dossier.response.TypeDossierResponse;
import lombok.Data;

@Data
public class DocSoumissionResponse {
    private Integer id;
    private String nom;
    private TypeDossierResponse typeDossier;
}
