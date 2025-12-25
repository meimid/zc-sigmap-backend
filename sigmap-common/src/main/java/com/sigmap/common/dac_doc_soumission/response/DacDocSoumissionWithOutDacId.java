package com.sigmap.common.dac_doc_soumission.response;

import com.sigmap.common.type_dossier.response.TypeDossierResponse;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DacDocSoumissionWithOutDacId {
    private Integer id;
    private String nom;
    private TypeDossierResponse typeDossier;
    private Date dateValidate;
}
