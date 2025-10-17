package com.sigmap.common.dac_doc_soumission.response;

import com.sigmap.common.config.TypeDossier;
import com.sigmap.common.doc_soumission.response.DocSoumissionResponse;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DacDocSoumissionWithOutDacId {
    private Integer id;
    private String nom;
    private TypeDossier typeDossier;
    private Date dateValidate;
}
