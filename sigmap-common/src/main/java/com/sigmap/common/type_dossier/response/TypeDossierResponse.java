package com.sigmap.common.type_dossier.response;

import lombok.Data;

@Data
public class TypeDossierResponse {
    private Integer id;

    private String code;

    private String libelle;

    private String description;
}
