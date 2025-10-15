package com.sigmap.common.type_agrement.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeAgrementRequest {

    private String libelle;
    private Integer organismeDeDeliveranceAgrementsId;
}
